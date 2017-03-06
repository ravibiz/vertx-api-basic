package com.vk.vertxapi.email;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import com.vk.vertxapi.api.LocalCache;
import com.vk.vertxapi.config.ConfigVerticle;
import com.vk.vertxapi.util.StringUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class EmailService extends AbstractVerticle
{
    private final static Logger LOG = LogManager.getLogger(EmailService.class);

    public static final String SEND_EMAIL = "send.email";
    private static final String SENDGRID_APIKEY = "SG.YbClTghsRTuan6jrIM5R6g.9NX0IHaaV7teje1IRRdehacSpfZwjeCnu2c1q08UO";

    private SendGrid sendgrid;

    private String sendFromUsername;
    
    @Override
    public void start(Future<Void> startFuture) throws Exception
    {
    	String apiKey = ConfigVerticle.getInstance().getConfigValue("sendgridkey").toString();
    	this.sendFromUsername  = ConfigVerticle.getInstance().getConfigValue("sendemailfromusername").toString();
        this.sendgrid = new SendGrid(apiKey);
        this.setupEmailProcessor();
        startFuture.complete();
    }	

    @Override
    public void stop() throws Exception
    {
        super.stop();
    }

    private void setupEmailProcessor()
    {
        EventBus eb = vertx.eventBus();
        eb.localConsumer(SEND_EMAIL, (Message<Integer> message) -> {
            this.sendEmail(message);

        }).completionHandler(res -> {
            LOG.info("EmailService registered." + res.succeeded());
        });

    }

    private void sendEmail(Message<Integer> message)
    {
        EmailData emailData = (EmailData) LocalCache.getInstance().remove(message.body());

        JsonObject emailContentJson = new JsonObject();
        
        emailContentJson.put("toemail", StringUtils.arrayToString(emailData.getToEmail(), ','));
        emailContentJson.put("fromemail", emailData.getFromEmail());
        emailContentJson.put("subject", emailData.getSubject());
        emailContentJson.put("subjecttemplate", emailData.getSubjectTemplate());
        emailContentJson.put("textmessage", emailData.getTextMessage());
        emailContentJson.put("bodytemplate", emailData.getBodyTemplate());
        emailContentJson.put("data", emailData.getData());

        
        SendGrid.Email email = new SendGrid.Email();
        
        email.addTo(emailData.getToEmail());
        email.setFromName(this.sendFromUsername);
        email.setFrom(emailData.getFromEmail());
        email.setSubject(emailData.getSubject());
        if (emailData.isHtmlBody())
        {
            email.setHtml(emailData.getMessageBody());
        }
        else
        {
            email.setText(emailData.getTextMessage());
        }

        try
        {
            SendGrid.Response response = sendgrid.send(email);
            LOG.info("Message sent status: " + response.toString());
        }
        catch (Exception e)
        {
            LOG.error("Error in sending email : " + emailData.toString(), e);
            message.fail(0, "Could not send email. " + e.getMessage());
        }
    }
    
    public static void main(String[] args)
    {
    	
    	String [] toEmails = {"ravikumar@abcd.com"};
    	String fromEmail = "ravikumar@abcd.com";
    	
    	EmailData emailData = new EmailData().setFromEmail(fromEmail).setToEmail(toEmails)
                .setHtmlBody(true)
                .setParams(new JsonObject().put("name", "Ravi").getMap())
                .setBodyTemplate("email/invitation.head.setup.body.vm")
                .setSubjectTemplate("email/invitation.head.setup.subject.vm");

        SendGrid sendgrid = new SendGrid("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");
        SendGrid.Email email = new SendGrid.Email();
        
        email.setFromName("Vertx API");
        email.addTo(emailData.getToEmail());
        
        email.setFrom(emailData.getFromEmail());
        email.setSubject(emailData.getSubject());
        email.setHtml(emailData.getMessageBody());

        try
        {
            SendGrid.Response response = sendgrid.send(email);
            LOG.info("Message sent status: " + response.getMessage());
        }
        catch (SendGridException e)
        {
            LOG.error("Error in sending email.", e);
        }
    }
}
