/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from "react";
import Paper from 'material-ui/Paper';
import {MenuItem, RaisedButton, SelectField, TextField} from "material-ui";

const items = [
    <MenuItem key={1} value={"code"} primaryText="code"/>,
    <MenuItem key={2} value={"id_token"} primaryText="id_token"/>,
    <MenuItem key={3} value={"id_token token"} primaryText="id_token token"/>,
];

const item = [
    <MenuItem key={1} value={"id_token"} primaryText="id_token"/>,
    <MenuItem key={2} value={"id_token token"} primaryText="id_token token"/>,
];

/**
 * This includes the forms that initially collects
 * the details required to run the tests.
 */
export class FirstForm extends React.Component {

    constructor(props) {
        super();
        this.state = {
            response_type: "",
            clientId: "",
            scope: "",
            callBack: "",
            authEndpoint: "",
            pro_type: "",
            nonce: "",
            value: null,

            client_secret: "",
            tokenEnd: "",
            data: {},

            isStarted: false,
        };
        this.handleStartBasic = this.handleStartBasic.bind(this);
        this.handleStartImplicit = this.handleStartImplicit.bind(this);
    }

    // The following methods handle the state changes in the text fields.
    handleChange = (event, index, value) => this.setState({value});

    handleClientIdChanged(event) {
        this.setState({clientId: event.target.value});
    }

    handleScopeChanged(event) {
        this.setState({scope: event.target.value});
    }

    handleCallbackChanged(event) {
        this.setState({callBack: event.target.value});
    }

    handleAuthEndpointChanged(event) {
        this.setState({authEndpoint: event.target.value});
    }

    handleSecretChanged(event) {
        this.setState({client_secret: event.target.value});
    }

    handleNonceChanged(event) {
        this.setState({nonce: event.target.value});
    }

    handleStartBasic(e) {
        e.preventDefault();
        this.setState({isStarted: true});
        sessionStorage.proType = "Basic";
        this.setState({pro_type: "Basic"});
    }

    handleStartImplicit(e) {
        e.preventDefault();
        this.setState({isStarted: true});
        sessionStorage.proType = "Implicit";
        this.setState({pro_type: "Implicit"});
    }

    //Sends the Authorization request to the Authorization endpoint.
    handleSubmit = (e) => {

        e.preventDefault();
        let response_type = this.state.value;
        let clientId = this.state.clientId;
        let scope = this.state.scope;
        let callBack = this.state.callBack;
        let authEndpoint = this.state.authEndpoint;
        let tokenEnd = this.state.tokenEnd;
        let client_secret = this.state.client_secret;
        let nonce = this.state.nonce;

        //The values of the text fields are store in sessionStorage for the later usage.
        sessionStorage.clientId = clientId;
        sessionStorage.scope = scope;
        sessionStorage.response_type = response_type;
        sessionStorage.callBack = callBack;
        sessionStorage.authEndpoint = authEndpoint;
        sessionStorage.client_secret = client_secret;
        sessionStorage.token_end = tokenEnd;
        sessionStorage.nonce = nonce

        if (this.state.pro_type == "Basic") {
            var load = 'response_type=' + response_type + '&client_id=' + clientId + '&scope=' + scope + '&redirect_uri=' + callBack + '&Use_pkce=no';
            window.location.href = "https://localhost:9443/oauth2/authorize?" + load;

        } else {
            var data = 'response_type=' + response_type + '&client_id=' + clientId + '&redirect_uri=' + callBack + '&nonce=' + nonce + '&scope=' + scope;
            var url = authEndpoint + "?" + data;
            sessionStorage.requestUrl = url;
            window.location.href = "https://localhost:9443/oauth2/authorize?" + data;
        }
    }

    render() {

        const styles = {
            customWidth: {
                width: 250,
            },

            block: {
                maxWidth: 250,
            },

            radioButton: {
                marginBottom: 5,
                marginLeft: 30,
            },

            checkbox: {
                marginBottom: 16,
            },

            margin: 12,

            paperStyle1: {
                height: 650,
                width: 500,
                //margin: 50,
                textAlign: 'center',
                display: 'inline-block',
                align: 'center',
            },

            paperStyle: {
                height: 600,
                width: 500,
                //margin: 50,
                textAlign: 'center',
                display: 'inline-block',
                align: 'center',
            },

        };

        if (!this.state.isStarted) {
            return (
                <div className="container">
                    <br/><br/>
                    <div className="jumbotron">
                        <h3>Welcome To OIDC Compliance Test Suite </h3><br/>
                        You can test your desired OIDC flows with any of the attributes you want. Also if you
                        wish to customize the initial attributes and test again, you simply have to edit the data set
                        and rerun the test suite.<br/>To Start the
                        test suite, select one of the following flows and press <b>"START"</b>
                    </div>
                    <br/>

                    <div className="panel panel-primary">
                        <div className="panel-heading">OIDC Authorization Code Flow</div>
                        <div className="panel-body">

                            <p>By clicking <b>"START"</b> you can start running the test suite and test all the steps
                                involved in
                                the <i><b>Authorization Code flow</b></i>.
                                <button type="button" className="btn btn-primary pull-right"
                                        onClick={this.handleStartBasic}>START
                                </button>
                                <br/><br/></p>
                            <p>To view more details on this test..
                                <a type="button" className="btn btn-default pull-right" data-toggle="collapse"
                                   data-target="#basic" aria-expanded="false" aria-controls="basic">
                                    <img src="./Images/down.png" height="15" width="15"/> More</a></p><br/>

                            <div className="collapse" id="basic">
                                <p>This section helps you to test the Authorization Code flow.The authorization
                                    code flow return an authorization code, which can be exchanged for an ID token.
                                    In this way all the tokens will be prevented from being exposed to the
                                    browser or the end user. The OpenId Connect Provider should authenticate the Relying
                                    Party
                                    before issuing the authorization code. <br/><br/>
                                </p>
                            </div>
                        </div>
                    </div>
                    <br/>
                    <div className="panel panel-primary">
                        <div className="panel-heading">OIDC Implicit Flow</div>
                        <div className="panel-body">
                            <p>By clicking <b>"START"</b> you can start running the test suite and test all the steps
                                involved in
                                the <i><b>Implicit flow</b></i>.
                                <button type="button" className="btn btn-primary pull-right"
                                        onClick={this.handleStartImplicit}>START
                                </button>
                                <br/><br/></p>
                            <p>To view more details on this test..
                                <button type="button" className="btn btn-default pull-right" data-toggle="collapse"
                                        data-target="#implicit" aria-expanded="false" aria-controls="basic">
                                    <img src="./Images/down.png" height="15" width="15"/> More
                                </button>
                            </p>
                            <br/>
                            <div className="collapse" id="implicit">
                                <p>This section helps you to test the Implicit flow. The implicit flow will directly
                                    return
                                    the ID token and/or access token to the Relying Party. In this way the token will be
                                    exposed to the end user. Unlike in the authorization code flow, the OpenId Connect
                                    Provider does not authenticate the Relying Party in this flow, because the token
                                    endpoint is not accessed during the process.<br/><br/></p></div>
                        </div>
                    </div>
                </div>
            );

        } else if (this.state.isStarted && this.state.pro_type == "Basic") {
            return (
                <form onSubmit={this.handleSubmit}>
                    <div className="container">
                        <br/><br/>
                        <h3>Test Basic Client Profile</h3>
                        <p><b>Please Enter the Following Details.</b></p><br/>
                        <Paper style={styles.paperStyle1} zDepth={5}>
                            <br/><br/>
                            <TextField
                                hintText="Client ID"
                                floatingLabelText="Client key provided by the OP"
                                floatingLabelFixed={true}
                                onChange={this.handleClientIdChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="openid"
                                floatingLabelText="Scope"
                                floatingLabelFixed={true}
                                onChange={this.handleScopeChanged.bind(this)}
                            />
                            <br/>
                            <SelectField
                                value={this.state.value}
                                onChange={this.handleChange}
                                hintText="Response Type"
                            >
                                {items}
                            </SelectField>
                            <br/>
                            <TextField
                                hintText="Callback URL"
                                floatingLabelText="Redirection URL after authorization"
                                floatingLabelFixed={true}
                                onChange={this.handleCallbackChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="Authorization endpoint"
                                floatingLabelText="Authorization endpoint of the OP"
                                floatingLabelFixed={true}
                                onChange={this.handleAuthEndpointChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="Client Secret"
                                floatingLabelText="Client secret generated by OP"
                                floatingLabelFixed={true}
                                onChange={this.handleSecretChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="Token endpoint"
                                floatingLabelText="Token endpoint of the OP"
                                floatingLabelFixed={true}
                                onChange={this.handleAuthEndpointChanged.bind(this)}
                            />
                            <br/><br/>
                            <RaisedButton
                                label="Initiate"
                                primary={true}
                                type="submit"
                            /><br/>
                        </Paper>
                    </div>
                </form>
            );
        } else {
            return (
                <form onSubmit={this.handleSubmit}>
                    <div className="container">
                        <br/><br/>
                        <h3>Test Implicit Client Profile</h3>
                        <p><b>Please Enter the Following Details.</b></p><br/>
                        <Paper style={styles.paperStyle} zDepth={5}>
                            <br/><br/><br/>
                            <TextField
                                hintText="Client ID"
                                floatingLabelText="Client key provided by the OP"
                                floatingLabelFixed={true}
                                onChange={this.handleClientIdChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="openid"
                                floatingLabelText="Scope"
                                floatingLabelFixed={true}
                                onChange={this.handleScopeChanged.bind(this)}
                            />
                            <br/>
                            <SelectField
                                value={this.state.value}
                                onChange={this.handleChange}
                                hintText="Response Type"
                            >
                                {item}
                            </SelectField>
                            <br/>
                            <TextField
                                hintText="Callback URL"
                                floatingLabelText="Redirection URL after authorization"
                                floatingLabelFixed={true}
                                onChange={this.handleCallbackChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="Authorization endpoint"
                                floatingLabelText="Authorization endpoint of the OP"
                                floatingLabelFixed={true}
                                onChange={this.handleAuthEndpointChanged.bind(this)}
                            />
                            <br/>
                            <TextField
                                hintText="Nonce"
                                floatingLabelText="Nonce value(any string)"
                                floatingLabelFixed={true}
                                onChange={this.handleNonceChanged.bind(this)}
                            />
                            <br/><br/><br/>
                            <RaisedButton
                                label="Initiate"
                                primary={true}
                                type="submit"
                            />
                        </Paper>
                    </div>
                </form>
            );
        }
    }
}
