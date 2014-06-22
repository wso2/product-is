import org.wso2.carbon.identity.sso.agent.openid.AttributesRequestor;

import java.util.*;

public class SampleAttributesRequestor implements AttributesRequestor {

    List<String> requestedAttributes = new ArrayList<String>();
    Map<String, Boolean> requiredMap = new HashMap<String, Boolean>();
    Map<String, String> typeURIMap = new HashMap<String, String>();
    Map<String, Integer> countMap = new HashMap<String, Integer>();

    public void init() {
        requestedAttributes.add("nickname");
        requiredMap.put("nickname", true);
        typeURIMap.put("nickname","http://axschema.org/namePerson/first");
        countMap.put("nickname",1);
        requestedAttributes.add("lastname");
        requiredMap.put("lastname", true);
        typeURIMap.put("lastname","http://axschema.org/namePerson/last");
        countMap.put("lastname",1);
        requestedAttributes.add("email");
        requiredMap.put("email", true);
        typeURIMap.put("email","http://axschema.org/contact/email");
        countMap.put("email",0);
        requestedAttributes.add("country");
        requiredMap.put("country", true);
        typeURIMap.put("country","http://axschema.org/contact/country/home");
        countMap.put("country",1);
        requestedAttributes.add("dob");
        requiredMap.put("dob", true);
        typeURIMap.put("dob","http://axschema.org/birthDate");
        countMap.put("dob",1);
    }

    public String[] getRequestedAttributes(String s) {
        String[] attrArray = new String[requestedAttributes.size()];
        return requestedAttributes.toArray(attrArray);
    }

    public boolean isRequired(String s, String s2) {
        return requiredMap.get(s2);
    }

    public String getTypeURI(String s, String s2) {
        return typeURIMap.get(s2);
    }

    public int getCount(String s, String s2) {
        return countMap.get(s2);
    }
}
