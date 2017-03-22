package ru.ppsrk.gwt.client.controls;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.SuggestOracle;

import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.controls.DTOSuggestBox.RemoteSuggestionCallback;
import ru.ppsrk.gwt.client.controls.DTOSuggestBox.Suggestable;

public class DTOSuggestOracle<T extends Suggestable> extends SuggestOracle {

    public interface MatcherCallback<T extends Suggestable> {
        public boolean matches(T suggestion, String request);
    }

    private class SuggestionCallback extends MyAsyncCallback<List<T>> {

        private Callback callback;
        private Request request;

        public SuggestionCallback(Request request, Callback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void onSuccess(List<T> result) {
            submitSuggestions(result, request, callback);
        }

    }

    private List<T> suggestions = new ArrayList<>();
    private MatcherCallback<T> matcher = new MatcherCallback<T>() {

        @Override
        public boolean matches(T suggestion, String request) {
            String[] reqArr = request.split("[ \t]");
            String[] suggArr = suggestion.getListboxValue().split("[ \t]");
            if (reqArr.length < 1 || suggArr.length < 1) {
                return false;
            }
            int reqI = 0;
            boolean matches = false;
            for (String sugg : suggArr) {
                if (!sugg.isEmpty()) {
                    // skip empty request parts
                    while (reqI < reqArr.length && reqArr[reqI].isEmpty()) {
                        ++reqI;
                    }
                    if (reqI < reqArr.length && sugg.toLowerCase().startsWith(reqArr[reqI].toLowerCase())) {
                        ++reqI;
                        if (reqI == reqArr.length) {
                            matches = true;
                            break;
                        }
                    }
                }
            }
            return matches;
        }
    };
    private RemoteSuggestionCallback<T> rsCallback = null;

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        if (rsCallback != null) {
            rsCallback.requestRemoteSuggestions(request, new SuggestionCallback(request, callback));
        } else {
            submitSuggestions(suggestions, request, callback);
        }
    }

    private void submitSuggestions(List<T> suggestionsList, Request request, Callback callback) {
        List<DTOSuggestion<T>> result = new ArrayList<>();
        for (T suggestion : suggestionsList) {
            String requestStr = request.getQuery();
            if (matcher.matches(suggestion, requestStr)) {
                result.add(new DTOSuggestion<T>(suggestion, requestStr));
            }
        }
        callback.onSuggestionsReady(request, new Response(result));
    }

    public void setSuggestions(List<T> suggestions) {
        this.suggestions = suggestions;
    }

    public void setMatcher(MatcherCallback<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    public void setRemoteSuggestionCallback(RemoteSuggestionCallback<T> rsCallback) {
        this.rsCallback = rsCallback;
    }

}