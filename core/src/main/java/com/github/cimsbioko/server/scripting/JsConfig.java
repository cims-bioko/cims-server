package com.github.cimsbioko.server.scripting;

import com.github.cimsbioko.server.dao.*;
import com.github.cimsbioko.server.domain.Individual;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.GeometryService;
import com.github.cimsbioko.server.service.StoredProcService;
import org.json.JSONObject;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import static java.util.Collections.*;

public class JsConfig implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(JsConfig.class);

    private static final String INIT_MODULE = "init";

    private final URLClassLoader loader;

    private ApplicationContext ctx;
    private DatabaseExport export;
    private Map<String, FormProcessor> formProcessors;

    public JsConfig(File file, ApplicationContext ctx) throws MalformedURLException {
        if (!file.canRead()) {
            throw new IllegalArgumentException("config file doesn't exist or is unreadable");
        }
        String base = "jar:file:" + file.getPath() + "!/";
        URL[] urls = {new URL(base + "server/"), new URL(base + "shared/")};
        this.loader = URLClassLoader.newInstance(urls);
        this.ctx = ctx;
    }

    public JsConfig load() throws URISyntaxException {
        Context ctx = Context.enter();
        try {
            ScriptableObject scope = buildScope(ctx);
            installConstants(scope);
            Require require = enableJsModules(ctx, scope);
            log.debug("loading init module");
            Scriptable init = require.requireMain(ctx, INIT_MODULE);
            export = ScriptableObject.getTypedProperty(init, "dbExport", DatabaseExport.class);
            formProcessors = ScriptableObject.getTypedProperty(init, "formProcessors", Map.class);
            return this;
        } finally {
            Context.exit();
        }
    }

    public DatabaseExport getDatabaseExport() {
        return export;
    }

    public Map<String, FormProcessor> getFormProcessors() {
        return formProcessors;
    }

    private static ScriptableObject buildScope(Context ctx) {
        return ctx.initSafeStandardObjects();
    }

    private void installConstants(ScriptableObject scope) {
        installJavaAdapter(scope);
        installInterfaces(scope);
        installUtilityClasses(scope);
        putConst(scope, "storedProcService", ctx.getBean(StoredProcService.class));
        putConst(scope, "locationHierarchyRepo", ctx.getBean(LocationHierarchyRepository.class));
        putConst(scope, "locationHierarchyLevelRepo", ctx.getBean(LocationHierarchyLevelRepository.class));
        putConst(scope, "locationRepo", ctx.getBean(LocationRepository.class));
        putConst(scope, "fieldWorkerRepo", ctx.getBean(FieldWorkerRepository.class));
        putConst(scope, "individualRepo", ctx.getBean(IndividualRepository.class));
        putConst(scope, "geometryService", ctx.getBean(GeometryService.class));
        putConst(scope, "log", log);
    }

    private void installJavaAdapter(ScriptableObject scope) {
        new LazilyLoadedCtor(scope, "JavaAdapter", "org.mozilla.javascript.JavaAdapter", false);
    }

    private static void putConst(ScriptableObject scope, String name, Object object) {
        scope.putConst(name, scope, object);
    }

    private static void installUtilityClasses(ScriptableObject scope) {
        putClasses(scope, Calendar.class, ConstraintViolations.class, JSONObject.class, Location.class, LocationHierarchy.class, Individual.class);
    }

    private static void installInterfaces(ScriptableObject scope) {
        putClasses(scope, DatabaseExport.class, FormProcessor.class);
    }

    private static void putClasses(ScriptableObject scope, Class... classes) {
        for (Class c : classes) {
            putClass(scope, c);
        }
    }

    private static void putClass(ScriptableObject scope, Class clazz) {
        scope.putConst(clazz.getSimpleName(), scope, new NativeJavaClass(scope, clazz));
    }

    private Require enableJsModules(Context ctx, ScriptableObject scope) throws URISyntaxException {
        RequireBuilder rb = new RequireBuilder()
                .setSandboxed(true)
                .setModuleScriptProvider(
                        new SoftCachingModuleScriptProvider(
                                new NonCachingModuleSourceProvider(getJsModulePath())));
        Require require = rb.createRequire(ctx, scope);
        require.install(scope);
        return require;
    }

    private List<URI> getJsModulePath() throws URISyntaxException {
        if (loader != null) {
            List<URI> uris = new ArrayList<>();
            for (URL u : loader.getURLs()) {
                uris.add(u.toURI());
            }
            return unmodifiableList(uris);
        }
        return emptyList();
    }

    public InputStream getResource(String path) {
        URL url = loader.getResource(path);
        if (url == null) {
            return null;
        }
        try {
            return getUncachedInputStream(url);
        } catch (IOException e) {
            log.warn("failed to load resource", e);
        }
        return null;
    }

    private static InputStream getUncachedInputStream(URL url) throws IOException {
        return getUncachedUrlConnection(url).getInputStream();
    }

    private static URLConnection getUncachedUrlConnection(URL url) throws IOException {
        URLConnection c = url.openConnection();
        c.setUseCaches(false);
        return c;
    }

    @Override
    public void close() throws IOException {
        ctx = null;
        loader.close();
        export = null;
        formProcessors = null;
    }

    private static class NonCachingModuleSourceProvider extends UrlModuleSourceProvider {

        NonCachingModuleSourceProvider(Iterable<URI> privilegedUris) {
            super(privilegedUris, null);
        }

        @Override
        protected URLConnection openUrlConnection(URL url) throws IOException {
            return getUncachedUrlConnection(url);
        }
    }
}
