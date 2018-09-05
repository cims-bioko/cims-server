package com.github.cimsbioko.server.webapi.odk;

public class Constants {

    /* shared by form and submission endpoints */
    public static final String ODK_API_PATH = "/odk";
    public static final String DEFAULT_VERSION = "1";
    public static final String MD5_SCHEME = "md5:";
    public static final String ID = "id";
    public static final String VERSION = "version";

    /* related to forms */
    public static final String HEAD = "head";
    public static final String MODEL = "model";
    public static final String INSTANCE = "instance";
    public static final String MANIFEST = "manifest";
    public static final String MEDIA_FILE = "mediaFile";
    public static final String FILENAME = "filename";
    public static final String HASH = "hash";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String MEDIA_MANIFEST = ".media-manifest.xml";
    public static final String FORM_DEF_FILE = "form_def_file";
    public static final String XLSFORM_DEF_FILE = "xlsform_def_file";
    public static final String TITLE = "title";

    /* related to submissions */
    public static final String INSTANCE_ID = "instanceID";
    public static final String COLLECTION_DATE_TIME = "collectionDateTime";
    public static final String SUBMISSION_DATE = "submissionDate";
    public static final String META = "meta";
    public static final String CIMS_BINDING = "cims-binding";
    public static final String XML_SUBMISSION_FILE = "xml_submission_file";
    public static final String DEVICE_ID = "deviceID";
}
