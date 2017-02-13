package com.github.cimsbioko.server.webservice.xforms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "xform", namespace = "http://openrosa.org/xforms/xformsList")
@XmlAccessorType(XmlAccessType.FIELD)
public class XForm {

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String formId;

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String name;

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String majorMinorVersion;

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String version;

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String hash;

    @XmlElement(namespace = "http://openrosa.org/xforms/xformsList")
    String downloadUrl;

    public XForm() {
    }
}
