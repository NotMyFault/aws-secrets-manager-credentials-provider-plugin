package io.jenkins.plugins.credentials.secretsmanager.config.fields.id.replaceFirst;

import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckTransformationApiIT extends CheckTransformationIT {

    @Override
    protected FormValidationResult validate(String regex, String replacement) {
        final JenkinsRule.JSONWebResponse response = doPost(
                String.format("descriptorByName/io.jenkins.plugins.credentials.secretsmanager.config.transformer.ReplaceFirst/testTransformation?regex=%s&replacement=%s", regex, replacement),
                "");

        final String responseBodyString = response.getContentAsString(StandardCharsets.UTF_8);
        final ParsedBody parsedBody = getValidationMessage(responseBodyString);

        if (parsedBody.status.equals("ok")) {
            return FormValidationResult.success(parsedBody.msg);
        } else {
            return FormValidationResult.error(parsedBody.msg);
        }
    }

    private JenkinsRule.JSONWebResponse doPost(String path, Object json) {
        try {
            return jenkins.postJSON(path, json);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParsedBody getValidationMessage(String body) {
        final String[] lines = body.split("<br>");
        final Pattern p = Pattern.compile("<div class=(.+)><img.+>(.+)");
        final Matcher matcher = p.matcher(lines[0]);
        if (matcher.find()) {
            final String status = matcher.group(1);
            final String msg = matcher.group(2);
            return new ParsedBody(msg, status);
        } else {
            throw new RuntimeException("Could not parse response body");
        }
    }

    private static class ParsedBody {
        final String msg;
        final String status;

        private ParsedBody(String msg, String status) {
            this.msg = msg;
            this.status = status;
        }
    }
}
