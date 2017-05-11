package org.marketsuite.component.xml;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

//base class for responses, converting some part of XML into variables
public class Response extends Message {
    public Response(Document document) {
        this.document = document;
        init();
    }

    private void init() {
        Element rootElement = document.getRootElement();
        String name = rootElement.getName();
        if (name.equals(RESPONSE)) {
            String status = rootElement.getAttributeValue(STATUS);
            _sWarning = rootElement.getAttributeValue(WARNING);
            if (status != null && status.equals(ERROR)) {
                Element element = document.getRootElement().getChild(REASON);
                if (element != null) {
                    _sStatusCode = element.getAttributeValue(ERR_CODE);
                }
            }
            //only set platform variable for login request
            _sPlatform = document.getRootElement().getAttributeValue(PLATFORM_NAME);
        }
    }

    public Element getResponseElement() {
        Element retElement = null;
        Element root = document.getRootElement();
        List children = root.getChildren();
        if (children.size() > 0) {
            retElement = (Element) children.get(0);
        }
        return retElement;
    }

    public Element getResponseElement(String name) {
        Element root = document.getRootElement();
        return root.getChild(name);
    }

    public String getSessionId() {
        Element resElement = getResponseElement();
        String sessionId = resElement.getAttributeValue(SESSION_ID);
        return sessionId;
    }

    public String getTime() {
        return document.getRootElement().getAttributeValue(TIME);
    }

    public boolean isError() {
        if (_sStatusCode != null) {
            return true;
        }
        return false;
    }

    public String getResponceMessage() {
        String msg = "success";
        if (document != null) {
            Element root = document.getRootElement();
            if (root != null) {
                Element resonElement = root.getChild(REASON);
                if (resonElement != null) {
                    msg = resonElement.getText();
                }
            }
        }
        return msg;
    }

    public String getWarningMessage() {
        if (_sWarning != null) {
            return _sWarning;
        }
        return "";
    }

    public boolean isWarning() {
        if (_sWarning != null && _sWarning.trim().length() > 0) {
            return true;
        }
        return false;
    }

    public String getCategory() {
        return document.getRootElement().getAttributeValue(CATEGORY);
    }

    public String getErrorCode() {
        return _sStatusCode;
    }

    public String getPlatform() {
        return _sPlatform;
    }

    //instance variables
    private String _sStatusCode;
    private String _sWarning;
    private String _sPlatform;

    //literals - todo: this part taken from controller's IMessage...........
    public static final String RESPONSE = "response";
    public static final String CATEGORY = "category";
    public static final String REASON = "reason";
    public static final String TIME = "time";
    public static final String SESSION_ID = "session-id";
    public static final String PLATFORM_NAME = "platform-name";
    public static final String STATUS = "status";
    public static final String WARNING = "warning";
    public static final String ERROR = "error";
    public static final String ERR_CODE = "err-code";
    public static final String PATH = "path";
    public static final String ID = "id";
}