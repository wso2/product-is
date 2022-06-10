package org.wso2.identity.integration.test.utils;

/**
 * An enum to represent supported password hashing mechanisms. Currently on MD 5 and SHA digests
 * are supported.
 */
public enum PasswordAlgorithm {

    PLAIN_TEXT("Plaintext"),
    MD5("MD5"),
    SHA("SHA");

    private String algorithmName;

    private PasswordAlgorithm(String algorithm) {
        this.algorithmName = algorithm;
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }

}
