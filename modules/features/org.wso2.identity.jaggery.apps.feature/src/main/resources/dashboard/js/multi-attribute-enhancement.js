/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Multi-Attribute Login Enhancement
 * 
 * Progressive enhancement for WSO2 Identity Server management console
 * that improves the multi-attribute login configuration UI by replacing
 * manual claim URI input with a dropdown selector.
 */
(function() {
    'use strict';
    
    var CONFIG = {
        CLAIM_API_URL: '/api/server/v1/claim-dialects/local/claims',
        FIELD_SELECTORS: [
            'input[name*="allowedattributes"]',
            'input[id*="allowedattributes"]',
            'input[name*="multiattribute"]',
            '#account\\.multiattributelogin\\.handler\\.allowedattributes'
        ],
        ENHANCEMENT_CLASS: 'multi-attr-enhanced',
        RETRY_INTERVAL: 1000,
        MAX_RETRIES: 10
    };
    
    var retryCount = 0;
    
    function initialize() {
        if (!isMultiAttributeLoginPage()) {
            return;
        }
        
        var inputField = findAllowedAttributesField();
        if (!inputField) {
            if (retryCount < CONFIG.MAX_RETRIES) {
                retryCount++;
                setTimeout(initialize, CONFIG.RETRY_INTERVAL);
                return;
            }
            return;
        }
        
        if (inputField.classList.contains(CONFIG.ENHANCEMENT_CLASS)) {
            return;
        }
        
        enhanceMultiAttributeField(inputField);
    }
    
    function isMultiAttributeLoginPage() {
        var url = window.location.href.toLowerCase();
        var pageContent = document.body.textContent.toLowerCase();
        
        return (url.indexOf('identity-providers') !== -1 || url.indexOf('resident') !== -1) &&
               (pageContent.indexOf('multi attribute login') !== -1 || 
                pageContent.indexOf('multiattribute') !== -1 ||
                pageContent.indexOf('allowed attribute') !== -1);
    }
    
    function findAllowedAttributesField() {
        var i, selector, field;
        for (i = 0; i < CONFIG.FIELD_SELECTORS.length; i++) {
            selector = CONFIG.FIELD_SELECTORS[i];
            field = document.querySelector(selector);
            if (field) {
                return field;
            }
        }
        
        var labels = document.querySelectorAll('label');
        var label, fieldId, nearbyInput;
        for (i = 0; i < labels.length; i++) {
            label = labels[i];
            if (label.textContent.toLowerCase().indexOf('allowed attribute') !== -1) {
                fieldId = label.getAttribute('for');
                if (fieldId) {
                    field = document.getElementById(fieldId);
                    if (field) return field;
                }
                
                nearbyInput = label.parentElement.querySelector('input[type="text"]');
                if (nearbyInput) return nearbyInput;
            }
        }
        
        return null;
    }
    
    function enhanceMultiAttributeField(inputField) {
        try {
            inputField.classList.add(CONFIG.ENHANCEMENT_CLASS);
            
            fetchAvailableClaims(function(claims) {
                if (!claims || claims.length === 0) {
                    return;
                }
                
                var dropdown = createClaimDropdown(claims, inputField.value);
                replaceInputWithDropdown(inputField, dropdown);
            });
            
        } catch (error) {
            console.error('WSO2 Enhancement: Error during enhancement:', error);
            inputField.classList.remove(CONFIG.ENHANCEMENT_CLASS);
        }
    }
    
    function fetchAvailableClaims(callback) {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', CONFIG.CLAIM_API_URL, true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.withCredentials = true;
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        var claims = JSON.parse(xhr.responseText);
                        var transformedClaims = claims.map(function(claim) {
                            return {
                                value: claim.claimURI,
                                label: (claim.displayName || claim.claimURI) + ' (' + claim.claimURI + ')',
                                displayName: claim.displayName || claim.claimURI,
                                uri: claim.claimURI
                            };
                        });
                        callback(transformedClaims);
                    } catch (error) {
                        console.error('WSO2 Enhancement: Error parsing claims:', error);
                        callback(getDefaultClaims());
                    }
                } else {
                    console.error('WSO2 Enhancement: API request failed:', xhr.status, xhr.statusText);
                    callback(getDefaultClaims());
                }
            }
        };
        
        xhr.onerror = function() {
            console.error('WSO2 Enhancement: Network error fetching claims');
            callback(getDefaultClaims());
        };
        
        xhr.send();
    }
    
    function getDefaultClaims() {
        return [
            {
                value: 'http://wso2.org/claims/username',
                label: 'Username (http://wso2.org/claims/username)',
                displayName: 'Username',
                uri: 'http://wso2.org/claims/username'
            },
            {
                value: 'http://wso2.org/claims/emailaddress',
                label: 'Email Address (http://wso2.org/claims/emailaddress)',
                displayName: 'Email Address',
                uri: 'http://wso2.org/claims/emailaddress'
            },
            {
                value: 'http://wso2.org/claims/givenname',
                label: 'First Name (http://wso2.org/claims/givenname)',
                displayName: 'First Name',
                uri: 'http://wso2.org/claims/givenname'
            },
            {
                value: 'http://wso2.org/claims/lastname',
                label: 'Last Name (http://wso2.org/claims/lastname)',
                displayName: 'Last Name',
                uri: 'http://wso2.org/claims/lastname'
            }
        ];
    }
    
    function createClaimDropdown(claims, currentValue) {
        var selectedClaims = currentValue ? 
            currentValue.split(',').map(function(uri) { return uri.trim(); }).filter(function(uri) { return uri; }) : 
            [];
        
        var container = document.createElement('div');
        container.className = 'multi-attr-dropdown-container';
        container.style.cssText = 'position: relative; min-width: 300px; border: 1px solid #ccc; border-radius: 4px; background: white;';
        
        var displayArea = document.createElement('div');
        displayArea.className = 'multi-attr-display';
        displayArea.style.cssText = 'padding: 8px; min-height: 20px; cursor: pointer; border-bottom: 1px solid #eee; background: #f9f9f9;';
        displayArea.innerHTML = '<em>Click to select claims...</em>';
        
        var dropdownList = document.createElement('div');
        dropdownList.className = 'multi-attr-list';
        dropdownList.style.cssText = 'display: none; max-height: 200px; overflow-y: auto; border-top: 1px solid #eee;';
        
        var i, claim, option, checkbox, label;
        for (i = 0; i < claims.length; i++) {
            claim = claims[i];
            option = document.createElement('label');
            option.style.cssText = 'display: block; padding: 8px; cursor: pointer; border-bottom: 1px solid #f0f0f0; font-size: 12px;';
            
            checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.value = claim.value;
            checkbox.checked = selectedClaims.indexOf(claim.value) !== -1;
            checkbox.style.marginRight = '8px';
            
            label = document.createElement('span');
            label.textContent = claim.label;
            
            option.appendChild(checkbox);
            option.appendChild(label);
            dropdownList.appendChild(option);
            
            checkbox.addEventListener('change', function() {
                updateSelection(container);
            });
        }
        
        var hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.value = currentValue;
        hiddenInput.name = 'selectedClaims';
        
        container.appendChild(displayArea);
        container.appendChild(dropdownList);
        container.appendChild(hiddenInput);
        
        displayArea.addEventListener('click', function() {
            var isVisible = dropdownList.style.display === 'block';
            dropdownList.style.display = isVisible ? 'none' : 'block';
        });
        
        document.addEventListener('click', function(e) {
            if (!container.contains(e.target)) {
                dropdownList.style.display = 'none';
            }
        });
        
        updateSelection(container);
        return container;
    }
    
    function updateSelection(container) {
        var checkboxes = container.querySelectorAll('input[type="checkbox"]');
        var displayArea = container.querySelector('.multi-attr-display');
        var hiddenInput = container.querySelector('input[type="hidden"]');
        
        var selectedClaims = [];
        var selectedLabels = [];
        var i, cb, label, displayName;
        
        for (i = 0; i < checkboxes.length; i++) {
            cb = checkboxes[i];
            if (cb.checked) {
                selectedClaims.push(cb.value);
                label = cb.nextSibling.textContent;
                displayName = label.split(' (')[0];
                selectedLabels.push(displayName);
            }
        }
        
        if (selectedClaims.length === 0) {
            displayArea.innerHTML = '<em>Click to select claims...</em>';
        } else {
            displayArea.innerHTML = 'Selected: ' + selectedLabels.join(', ');
        }
        
        hiddenInput.value = selectedClaims.join(',');
    }
    
    function replaceInputWithDropdown(originalInput, dropdown) {
        var hiddenInput = dropdown.querySelector('input[type="hidden"]');
        hiddenInput.name = originalInput.name;
        hiddenInput.id = originalInput.id;
        
        originalInput.parentNode.insertBefore(dropdown, originalInput);
        originalInput.style.display = 'none';
        originalInput.setAttribute('data-enhanced', 'true');
        
        var form = originalInput.closest('form');
        if (form) {
            form.addEventListener('submit', function() {
                originalInput.value = hiddenInput.value;
            });
        }
        
        setInterval(function() {
            if (originalInput.value !== hiddenInput.value) {
                originalInput.value = hiddenInput.value;
            }
        }, 1000);
    }
    
    // Add CSS styles
    var style = document.createElement('style');
    style.textContent = '.multi-attr-dropdown-container { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; } ' +
        '.multi-attr-display { transition: background-color 0.2s; } ' +
        '.multi-attr-display:hover { background-color: #f0f0f0 !important; } ' +
        '.multi-attr-list label:hover { background-color: #f8f8f8; } ' +
        '.multi-attr-list input[type="checkbox"] { transform: scale(1.1); }';
    document.head.appendChild(style);

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }
    
    // Also try to initialize after a short delay
    setTimeout(initialize, 2000);
    
})();