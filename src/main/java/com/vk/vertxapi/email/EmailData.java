package com.vk.vertxapi.email;

import com.vk.vertxapi.email.template.VelocityTemplateProcessor;
import com.vk.vertxapi.util.StringUtils;

import io.vertx.core.json.JsonObject;

public class EmailData
{
    private String[] toEmail;
    private String fromEmail;
    private String subject;
    private String textMessage;
    private String bodyTemplate;
    private String subjectemplate;
    private Object params;
    private boolean isHtmlBody;
    private JsonObject data;
    
    public String getMessageBody()
    {
        if (this.isHtmlBody() && !StringUtils.isEmpty(this.getBodyTemplate()))
        {
            return new VelocityTemplateProcessor().process(this.getBodyTemplate(), this.getParams());
        }
        else
        {
            return this.textMessage;
        }
    }

    public boolean isHtmlBody()
    {
        return isHtmlBody;
    }

    public EmailData setHtmlBody(boolean htmlBody)
    {
        isHtmlBody = htmlBody;
        return this;
    }

    public String[] getToEmail()
    {
        return toEmail;
    }

    public EmailData setToEmail(String[] toEmail)
    {
        this.toEmail = toEmail;
        return this;
    }

    public String getFromEmail()
    {
        return fromEmail;
    }

    public EmailData setFromEmail(String fromEmail)
    {
        this.fromEmail = fromEmail;
        return this;
    }

    public String getSubject()
    {
        if (!StringUtils.isEmpty(this.getSubjectTemplate()))
        {
            return new VelocityTemplateProcessor().process(this.getSubjectTemplate(), this.getParams());
        }
        return subject;
    }

    public EmailData setSubject(String subject)
    {
        this.subject = subject;
        return this;
    }

    public String getTextMessage()
    {
        return textMessage;
    }

    public EmailData setTextMessage(String textMessage)
    {
        this.textMessage = textMessage;
        return this;
    }

    public String getBodyTemplate()
    {
        return bodyTemplate;
    }

    public EmailData setBodyTemplate(String bodyTemplate)
    {
        this.bodyTemplate = bodyTemplate;
        return this;
    }

    public String getSubjectTemplate()
    {
        return subjectemplate;
    }

    public EmailData setSubjectTemplate(String subjectTemplate)
    {
        this.subjectemplate = subjectTemplate;
        return this;
    }

    public Object getParams()
    {
        return params;
    }

    public EmailData setParams(Object params)
    {
        this.params = params;
        return this;
    }

	public JsonObject getData() {
		return data;
	}

	public EmailData setData(JsonObject data) {
		this.data = data;
		return this;
	}
    
    
}
