package com.github.cimsbioko.server.webservice.xforms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "xforms", namespace = "http://openrosa.org/xforms/xformsList")
@XmlAccessorType(XmlAccessType.FIELD)
public class XFormList {

    @XmlElement(name = "xform", namespace = "http://openrosa.org/xforms/xformsList")
    List<XForm> forms = new ArrayList<>();

    public XFormList() {
    }

    public XFormList(XForm... forms) {
        for (XForm f : forms)
            this.forms.add(f);
    }
}
