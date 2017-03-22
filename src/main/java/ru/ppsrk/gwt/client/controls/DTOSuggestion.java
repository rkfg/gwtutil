package ru.ppsrk.gwt.client.controls;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import ru.ppsrk.gwt.client.controls.DTOSuggestBox.Suggestable;

public class DTOSuggestion<T extends Suggestable> implements Suggestion {

    private T dto;
    private String request;

    public DTOSuggestion(T dto, String request) {
        this.dto = dto;
        this.request = RegExp.quote(request);
    }

    @Override
    public String getDisplayString() {
        String[] reqArr = request.split("[ \t]");
        String suggestion = dto.getListboxValue();
        String[] suggArr = suggestion.split("[ \t]");
        if (reqArr.length < 1 || suggArr.length < 1) {
            return suggestion;
        }
        int reqI = 0;
        StringBuilder sb = new StringBuilder();
        for (String sugg : suggArr) {
            if (!sugg.isEmpty()) {
                while (reqI < reqArr.length && reqArr[reqI].isEmpty()) {
                    ++reqI;
                }
                if (reqI < reqArr.length) {
                    String reqElem = reqArr[reqI];
                    if (sugg.toLowerCase().startsWith(reqElem.toLowerCase())) {
                        sb.append(sugg.replaceFirst("(.{" + reqElem.length() + "})", "<b>$1</b>"));
                        ++reqI;
                    } else {
                        sb.append(sugg);
                    }
                } else {
                    sb.append(sugg);
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public String getReplacementString() {
        return dto.getReplacementString();
    }

    public T getDTO() {
        return dto;
    }
    
}