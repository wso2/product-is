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

import React from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import axios from 'axios';
import qs from 'qs';
import jwt_decode from 'jwt-decode';
import Highlight from 'react-highlight';
import {Dialog, FlatButton, MenuItem, Paper, SelectField, TextField} from "material-ui";

const implicitItems = [
    <MenuItem key={1} value={"none"} primaryText="none"/>,
    <MenuItem key={2} value={"id_token"} primaryText="id_token"/>,
    <MenuItem key={3} value={"id_token token"} primaryText="id_token token"/>,
];

const basicItems = [
    <MenuItem key={1} value={"none"} primaryText="none"/>,
    <MenuItem key={2} value={"code"} primaryText="code"/>,
];

export class OauthClient extends React.Component {

    constructor() {
        super();
        this.state = {
            client_key: sessionStorage.clientId,
            grantType: sessionStorage.grantType,
            scope: sessionStorage.scope,
            callback: sessionStorage.callBack,
            authzEndpoint: sessionStorage.authEndpoint,
            acTokenEndpoint: sessionStorage.token_end,
            secret: sessionStorage.client_secret,
            nonce: sessionStorage.nonce,
            value: sessionStorage.response_type,
            code: "",

            res_header: {},
            tokenSplit: "",
            idEn: {},
            header_decoded: "",
            body_decoded: "",
            sig_decoded: "",

            id_token: [],
            id_encoded: "",
            id_decoded: "",
            id_header: {},
            id_body: {},
            imp_id_header: {},
            imp_id_body: {},

            id_signature: "",

            isEdited: false,
            isLoaded: false,
            isImplicit: false,
            implicit_id: "",
            implicit_header: "",
            implicit_body: {},
            imp_result: "",
            access_token: "",
            responseUrl: "",
            res_data: [],

            error_des: "",
            err: "",
            stateText: " Not Started!!! ",
            isDisabled: true,
            status: "",
            status_Text: "",
            config: {},
            resultState: "",
            summary: "",
            open: false,
            openImp: false,
        };

        this.handleClientIdChanged = this.handleClientIdChanged.bind(this);
        this.handleScopeChanged = this.handleScopeChanged.bind(this);
        this.handleCallbackChanged = this.handleCallbackChanged.bind(this);
        this.handleAuthEndpointChanged = this.handleAuthEndpointChanged.bind(this);
        this.handleSecretChanged = this.handleSecretChanged.bind(this);
        this.handleTokenEndpointChanged = this.handleTokenEndpointChanged.bind(this);
        this.handleNonceChanged = this.handleNonceChanged.bind(this);

    }

    handleChange = (event, index, value) => this.setState({value});

    handleClientIdChanged(event) {
        this.setState({client_key: event.target.value});
    }

    handleCallbackChanged(event) {
        this.setState({callBack: event.target.value});
    }

    handleNonceChanged(event) {
        this.setState({nonce: event.target.value});
    }

    handleScopeChanged(event) {
        this.setState({scope: event.target.value});
    }

    handleAuthEndpointChanged(event) {
        this.setState({authzEndpoint: event.target.value});
    }

    handleTokenEndpointChanged(event) {
        this.setState({accessToken: event.target.value});
    }

    handleSecretChanged(event) {
        this.setState({secret: event.target.value});
    }

    handleOpen = (e) => {
        e.preventDefault();
        this.setState({open: true});
    };

    handleOpenImp = (e) => {
        e.preventDefault();
        this.setState({openImp: true});
    };

    handleClose = () => {
        this.setState({open: false});
        this.setState({openImp: false});
    };

    handleResubmitBasic = (e) => {

        e.preventDefault();
        sessionStorage.isEdited = this.state.isEdited;

        let client_key = this.state.client_key;
        let scope = this.state.scope;
        let callback = this.state.callback;
        let secret = this.state.secret;
        let authzEnd = this.state.authzEndpoint;
        let tokenEnd = this.state.acTokenEndpoint;
        let res_type = this.state.value;

        sessionStorage.isEdited = true;
        sessionStorage.newClientKey = client_key;
        sessionStorage.newCallback = callback;
        sessionStorage.newSecret = secret;
        sessionStorage.newAuthzEnd = authzEnd;
        sessionStorage.newTokenEnd = tokenEnd;

        var load = 'response_type=' + res_type + '&client_id=' + client_key + '&scope=' + scope + '&redirect_uri=' + callback + '&Use_pkce=no';

        window.location.href = "https://localhost:9443/oauth2/authorize?" + load;
    }

    handleResubmitImplicit = (e) => {

        e.preventDefault();
        sessionStorage.isEdited = this.state.isEdited;

        let client_key = this.state.client_key;
        let scope = this.state.scope;
        let callback = this.state.callback;
        let authzEnd = this.state.authzEndpoint;
        let response_type = this.state.value;
        let nonce = this.state.nonce;

        sessionStorage.response_type = response_type;

        var data = 'response_type=' + response_type + '&client_id=' + client_key + '&redirect_uri=' + callback + '&nonce=' + nonce + '&scope=' + scope;
        sessionStorage.requestUrl = authzEnd + "?" + data;
        window.location.href = authzEnd + "?" + data;
    }

    // Sends a request to token endpoint of the IS using the axios library.
    sendBasicProRequest = (e) => {
        e.preventDefault();

        this.setState({isDisabled: false});
        let client = "";
        let client_secret = "";
        let callback = "";

        let isEdited = sessionStorage.isEdited;

        if (!isEdited) {
            client = sessionStorage.clientId;
            client_secret = sessionStorage.client_secret;
            callback = sessionStorage.callBack;
        } else {
            client = sessionStorage.newClientKey;
            client_secret = sessionStorage.newSecret;
            callback = sessionStorage.newCallback;
        }


        let url = location.href;
        let br01 = url.split("&");
        let br02 = br01[0].split("=");

        let code = br02[1];

        // A string containing the ClientID and the Client secret is created.
        var string = client + ':' + client_secret;
        // The above string gets encoded.
        let encoded = btoa(string);

        const headers = {
            'Authorization': 'Basic ' + encoded,
            'Accept': 'application/x-www-form-urlencoded',
            'Content-Type': 'application/x-www-form-urlencoded',
        };

        // Request parameters are set to a JSON object.
        const params = {
            'grant_type': 'authorization_code',
            'code': code,
            'redirect_uri': callback,
        };

        // The following are the headers sent inorder to make a valid request to the token endpoint.
        axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
        axios.defaults.headers.common['Access-Control-Allow-Methods'] = 'post';
        axios.defaults.headers.common['Access-Control-Allow-Headers'] = 'Authorization, Content-Type';
        axios.defaults.headers.common['Authorization'] = 'Basic ' + encoded;
        axios.defaults.headers.common['Content-Type'] = 'application/x-www-form-urlencoded';
        axios.defaults.headers.common['Accept'] = 'application/json';

        // Using the post method in the axios library to send the request.
        axios.post('https://localhost:9443/oauth2/token', qs.stringify(params), headers)
            .then(response => {


                // The headers of the response are set to an object.
                let res_header = response.headers;
                this.setState({res_header});

                // The parameters of the response are set to an object.
                let res_data = response.data;
                this.setState({res_data})

                // The headers of the request are set to an object.
                let config = response.config.headers;
                this.setState({config})

                // Get the id token from the response.
                let tokenSplit = response.data.id_token;
                // Split the token so that we can decode the header and the payload of the token separately.
                var splitArr = tokenSplit.split(".");

                // The separated header and the payload of the ID token.
                let id_header = splitArr[0];
                let id_body = splitArr[1];

                // Decode the header.
                let header_decoded = atob(id_header);
                this.setState({header_decoded})

                // Decode the payload.
                let body_decoded = atob(id_body);
                this.setState({body_decoded})

                let id_token = response.data.id_token;
                this.setState({id_token})

                let idEn = jwt_decode(id_token);
                this.setState({idEn})

                this.validateResponse(response);
            })

            .catch(error => {

                    let stateStr = JSON.stringify(error.response.data, null, '\t');

                    let str1 = stateStr.split(",");
                    let str2 = str1[0].split(":");
                    let str3 = str1[1].split(":");

                    let error_des = str2[1];
                    let err = str3[1];

                    this.setState({error_des});
                    this.setState({err});

                    this.handleError(error);
                }
            );
    }

    viewBasicResults = (e) => {
        e.preventDefault();

        // isLoaded is set true.
        this.setState({isLoaded: true});
    }

    viewImplicitResults = (e) => {
        e.preventDefault();
        this.setState({isImplicit: true});
    }

    getImplicitProData = (e) => {
        e.preventDefault();

        this.setState({isDisabled: false});
        let imp_result = this.state.imp_result;
        let stateText = this.state.stateText;

        let url = location.href;
        this.setState({responseUrl: url});

        if (sessionStorage.response_type == "id_token token") {
            let br01 = url.split("&");
            let br02 = br01[0].split("=");
            let br03 = br01[1].split("=");

            let acc_token = br02[1];
            this.setState({access_token: acc_token});

            let id_token = br03[1];
            this.setState({implicit_id: id_token});
            sessionStorage.id = id_token;
        } else {
            let br01 = url.split("&");
            let br02 = br01[0].split("=");

            let id_token = br02[1];
            this.setState({implicit_id: id_token})
            sessionStorage.id = id_token;

            let acc_token = "Response Type: id_token. Therefore NO ACCESS TOKEN!!!"
            this.setState({access_token: acc_token})
        }

        // Split the token so that we can decode the header and the payload of the token separately.
        let splitToken = sessionStorage.id;
        var splitArr = splitToken.split(".");

        // The separated header and the payload of the ID token.
        var imp_id_header = splitArr[0];

        // Decode the header.
        let implicit_header = atob(imp_id_header);
        this.setState({implicit_header})

        // Decode the payload.
        let implicit_body = jwt_decode(sessionStorage.id);
        this.setState({implicit_body});

        if (sessionStorage.id !== null && this.state.implicit_header !== null) {
            imp_result = <h4><span className="label label-success">Success!</span></h4>;
            this.setState({imp_result});

            stateText = <font color="green"> Success!!!</font>
            this.setState({stateText});
        } else {
            imp_result = <h4><span className="label label-danger">Failed!</span></h4>;
            this.setState({imp_result});

            stateText = <font color="red"> Failed!!!</font>
            this.setState({stateText});
        }
    }

    // The response is validated depending on the status code of the request.
    validateResponse = (response) => {

        let status = this.state.status;
        let status_Text = this.state.status_Text;
        let resultState = this.state.resultState;
        let summary = this.state.summary;
        let stateText = this.state.stateText;


        status = response.status;
        this.setState({status})

        status_Text = response.statusText;
        this.setState({status_Text})

        if (response.status == 200) {
            resultState = <h4><span className="label label-success">Success!</span></h4>;
            this.setState({resultState});

            stateText = <font color="green"> Success!!!</font>
            this.setState({stateText});

            summary = status + " " + status_Text;
            this.setState({summary})
        }
    }

    // This method handle the client side whenever an error is returned in the response.
    handleError = (error) => {
        let resultState = this.state.resultState;
        let summary = this.state.summary;
        let state = this.state.status
        let stateText = this.state.stateText;


        let error_des = this.state.error_des;
        let err = this.state.err;

        if (error !== null && state !== 200) {
            resultState = <h4><span className="label label-danger">Failed!</span></h4>;
            this.setState({resultState})

            stateText = <font color="red"> Failed!!!</font>
            this.setState({stateText});

            summary = <font color="red">Error: {error_des} {err}</font>
            this.setState({summary})
        }
    }

    handleEdit = (e) => {
        e.preventDefault();
        this.setState({isLoaded: false});
        this.setState({isImplicit: false});
    }

    render() {

        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose}
            />,
            <RaisedButton
                label="UPDATE"
                primary={true}
                onClick={this.handleResubmitBasic.bind(this)}
            />,
        ];

        const actions01 = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose}
            />,
            <RaisedButton
                label="UPDATE"
                primary={true}
                onClick={this.handleResubmitImplicit.bind(this)}
            />,
        ];

        const styles = {
            paperStyle1: {
                height: 350,
                width: 400,
                margin: 20,
                textAlign: 'center',
                display: 'inline-block',
                align: 'center',
            },

            paperStyle2: {
                height: 200,
                width: 400,
                margin: 20,
                textAlign: 'center',
                display: 'inline-block',
                align: 'center',
            },

            buttons: {
                margin: 12,
            },
        };

        let res_data = this.state.res_data;
        let res_header = this.state.res_header;
        let status = this.state.status;
        let status_Text = this.state.status_Text;
        let config = this.state.config;
        let resultState = this.state.resultState;
        let imp_result = this.state.imp_result;
        let summary = this.state.summary;
        let id_token = this.state.id_token;

        let header_decoded = this.state.header_decoded;
        let body_decoded = this.state.body_decoded;

        let idEn = this.state.idEn;
        let implicit_body = this.state.implicit_body;

        var data = [];

        // The res_data object is assigned to an array.
        for (var i in res_data) {
            data.push(res_data[i]);
        }

        let acc_token = data[0];
        let ref_token = data[1];
        let res_scope = data[2];
        let id_encoded = data[3];
        let tok_type = data[4];
        let time = data[5];

        if (!this.state.isLoaded && !this.state.isImplicit) {
            if (sessionStorage.proType == "Basic") {
                return (
                    <div className="container">
                        <br/><br/>
                        <div className="panel panel-primary">
                            <div className="panel-heading">Configuration</div>
                            <div className="panel-body">
                                <div className="jumbotron">
                                    <a href="http://localhost:8080/authorize" className="btn btn-primary pull-right"
                                       role="button" data-toggle="tooltip" title="Edit Profile">
                                        <img src="./Images/refresh.png" height="15" width="15"/> &nbsp;Restart</a><br/>
                                    <b>Profile Type:</b> {sessionStorage.proType} <br/>
                                    <b>Scope:</b> {sessionStorage.scope}<br/>
                                    <b>Callback URL:</b> {sessionStorage.callBack}<br/>
                                    <b>Authorization Endpoint:</b> {sessionStorage.authEndpoint}
                                </div>
                            </div>
                        </div>
                        <div className="panel panel-primary">
                            <div className="panel-heading">Basic Flow</div>
                            <div className="panel-body">
                                <br/>
                                <p>1. Click the <b>"Test Results"</b> button to get a full report on the test you ran.
                                    The
                                    following details will be shown in the report page.<br/>
                                    <ul>
                                        <li>Request and Response Headers</li>
                                        <li>Response Pay Load</li>
                                        <li>Encoded Id token</li>
                                        <li>Decoded Id Token</li>
                                    </ul>

                                    2. Click the <b>"EDIT"</b> button to edit the attributes that were initially set and
                                    rerun
                                    the test suite. You&#x27;ll have to click the <b>"RUN"</b> button again, to run the test
                                    suite with
                                    the updated attributes.
                                </p>
                                <br/>
                                <div className="jumbotron">
                                    <b>Server Response Status:</b>{this.state.stateText}<br/><br/>
                                    <button type="button" className="btn btn-primary pull-left"
                                            onClick={this.viewBasicResults} disabled={this.state.isDisabled}><img
                                        src="./Images/document.png" height="17" width="17"/> Test Results
                                    </button>
                                    <br/><br/>
                                    <hr/>
                                    To run the test click the <b><i>"RUN"</i> </b> button.
                                    <button type="button" className="btn btn-secondary pull-right"
                                            onClick={this.handleOpen}><img src="./Images/edit.png" height="15"
                                                                           width="15"/>
                                        EDIT
                                    </button>
                                    <button type="button" className="btn btn-primary pull-right"
                                            onClick={this.sendBasicProRequest}><img src="./Images/run.png" height="15"
                                                                                    width="15"/> RUN
                                    </button>
                                </div>
                                <Dialog
                                    title="Please Edit the required fields"
                                    actions={actions}
                                    modal={true}
                                    open={this.state.open}
                                >
                                    <div className="panel panel-primary">
                                        <div className="panel-heading">Data set Customization</div>
                                        <div className="panel-body">
                                            <p><TextField
                                                floatingLabelText="Client ID"
                                                floatingLabelFixed={true}
                                                value={this.state.client_key}
                                                onChange={this.handleClientIdChanged}
                                            /><br/>
                                                <TextField
                                                    floatingLabelText="Scope"
                                                    floatingLabelFixed={true}
                                                    value={this.state.scope}
                                                    onChange={this.handleScopeChanged}
                                                /><br/>
                                                <TextField
                                                    floatingLabelText="Callback URL"
                                                    floatingLabelFixed={true}
                                                    value={this.state.callback}
                                                    onChange={this.handleCallbackChanged}
                                                /><br/>
                                                <SelectField
                                                    value={this.state.value}
                                                    onChange={this.handleChange}
                                                    hintText="Response Type"
                                                >
                                                    {basicItems}
                                                </SelectField><br/>
                                                <TextField
                                                    floatingLabelText="Client Secret"
                                                    floatingLabelFixed={true}
                                                    value={this.state.secret}
                                                    onChange={this.handleSecretChanged}
                                                /><br/>
                                                <TextField
                                                    floatingLabelText="Token Endpoint"
                                                    floatingLabelFixed={true}
                                                    value={this.state.acTokenEndpoint}
                                                    onChange={this.handleTokenEndpointChanged}
                                                /><br/><br/>
                                            </p>
                                        </div>
                                    </div>
                                </Dialog>
                            </div>
                        </div>
                    </div>
                );

            } else {
                return (
                    <div className="container">
                        <br/>
                        <div className="panel panel-primary">
                            <div className="panel-heading">Configuration</div>
                            <div className="panel-body">
                                <div className="jumbotron">
                                    <a href="http://localhost:8080/authorize" className="btn btn-primary pull-right"
                                       role="button" data-toggle="tooltip" title="Edit Profile">
                                        <img src="./Images/refresh.png" height="15" width="15"/> &nbsp;Restart</a><br/>
                                    <b>Profile Type:</b> {sessionStorage.proType} <br/>
                                    <b>Scope:</b> {sessionStorage.scope}<br/>
                                    <b>Callback URL:</b> {sessionStorage.callBack}<br/>
                                    <b>Authorization Endpoint:</b> {sessionStorage.authEndpoint}
                                </div>
                            </div>
                        </div>
                        <div className="panel panel-primary">
                            <div className="panel-heading">Implicit Flow</div>
                            <div className="panel-body">
                                <br/>
                                <p>1. Click the <b>"Test Results"</b> button to get a full report on the test you ran.
                                    The
                                    following details will be shown in the report page.<br/>
                                    <ul>
                                        <li>Authorization Request and Response</li>
                                        <li>Encoded Id token</li>
                                        <li>Decoded Id Token</li>
                                        <li>Access token (If requested)</li>
                                    </ul>

                                    2. Click the <b>"EDIT"</b> button to edit the attributes that were initially set and
                                    rerun
                                    the test suite.
                                </p>
                                <br/>
                                <div className="jumbotron">
                                    <b>Server Response Status:</b>{this.state.stateText}<br/><br/>
                                    <button type="button" className="btn btn-primary pull-left"
                                            onClick={this.viewImplicitResults} disabled={this.state.isDisabled}><img
                                        src="./Images/document.png" height="17" width="17"/> Test Results
                                    </button>
                                    <br/><br/>
                                    <hr/>
                                    To run the test click the <b><i>"RUN"</i> </b> button.
                                    <button type="button" className="btn btn-secondary pull-right"
                                            onClick={this.handleOpenImp}><img src="./Images/edit.png" height="15"
                                                                              width="15"/>
                                        EDIT
                                    </button>
                                    <button type="button" className="btn btn-primary pull-right"
                                            onClick={this.getImplicitProData}><img src="./Images/run.png" height="17"
                                                                                   width="17"/> RUN
                                    </button>
                                    <br/>
                                </div>
                                <Dialog
                                    title="Please Edit the required fields"
                                    actions={actions01}
                                    modal={true}
                                    open={this.state.openImp}
                                >
                                    <div className="panel panel-primary">
                                        <div className="panel-heading">Data set Customization</div>
                                        <div className="panel-body">
                                            <p><TextField
                                                floatingLabelText="Client ID"
                                                floatingLabelFixed={true}
                                                value={this.state.client_key}
                                                onChange={this.handleClientIdChanged}
                                            /><br/>
                                                <TextField
                                                    floatingLabelText="Scope"
                                                    floatingLabelFixed={true}
                                                    value={this.state.scope}
                                                    onChange={this.handleScopeChanged}
                                                /><br/>
                                                <SelectField
                                                    value={this.state.value}
                                                    onChange={this.handleChange}
                                                    hintText="Response Type"
                                                >
                                                    {implicitItems}
                                                </SelectField>
                                                <br/>
                                                <TextField
                                                    floatingLabelText="Nonce value (any string)"
                                                    floatingLabelFixed={true}
                                                    value={this.state.nonce}
                                                    onChange={this.handleNonceChanged}
                                                /><br/>
                                                <TextField
                                                    floatingLabelText="Callback URL"
                                                    floatingLabelFixed={true}
                                                    value={this.state.callback}
                                                    onChange={this.handleCallbackChanged}
                                                /><br/>
                                                <TextField
                                                    floatingLabelText="Authorization Endpoint"
                                                    floatingLabelFixed={true}
                                                    value={this.state.authzEndpoint}
                                                    onChange={this.handleAuthEndpointChanged}
                                                /><br/><br/></p>
                                        </div>
                                    </div>
                                </Dialog>
                            </div>
                        </div>
                    </div>
                );
            }

        } else if (this.state.isLoaded && !this.state.isImplicit) {
            return (
                <div className="container">
                    <div className="jumbotron">
                        <button type="button" className="btn btn-primary pull-right" onClick={this.handleEdit}>
                            <img src="./Images/left-arrow.png" height="17" width="17"/> &nbsp;Back
                        </button>
                        <br/>
                        <h4>The following results were generated during the test.</h4>
                        <b>Test Result:</b>{resultState}

                        <b>Summary of the Test: </b>
                        <br/>
                        Response status: {summary}<br/>Authorization: Basic<br/>Scope: Openid

                    </div>
                    <br/>
                    <div className="panel panel-primary">
                        <div className="panel-heading">Headers</div>
                        <div className="panel-body"><br/>
                            <h5>Request Headers</h5><p>
                                <Highlight className='javascript'>
                                    {JSON.stringify(config, null, '\t')}
                                </Highlight></p>
                            <br/>
                            <h5>Response Headers</h5>
                            <p>
                                <Highlight className='javascript'>
                                    {JSON.stringify(res_header, null, '\t')}
                                </Highlight><br/></p>
                        </div>
                    </div>
                    <div className="panel panel-primary">
                        <div className="panel-heading">Response Data</div>
                        <div className="panel-body">
                                <pre className="break">
                                <br/><p className="break">
                                access_token: "{acc_token}"<br/>
                                expires_in: "{time}"<br/>
                                id_token: "{id_encoded}"
                                <br/>
                                refresh_token: "{ref_token}"<br/>
                                scope: "{res_scope}"<br/>
                                token_type: "{tok_type}"<br/><br/></p>
                                </pre>
                        </div>
                    </div>
                    <div className="panel panel-primary">
                        <div className="panel-heading">Encoded ID Token</div>
                        <div className="panel-body">
                            <br/><Highlight className='javascript'>
                            {id_token}
                        </Highlight>
                        </div>
                    </div>
                    <div className="panel panel-primary">
                        <div className="panel-heading">Decoded ID Token</div>
                        <div className="panel-body">
                            <h5>HEADER: Algorithm and Token Type</h5>
                            <p>
                                <Highlight className='javascript'>
                                    {header_decoded}
                                </Highlight>
                            </p>
                            <br/>
                            <h5>PAYLOAD: Data</h5>
                            <p>
                                <Highlight className='javascript'>
                                    {JSON.stringify(idEn, null, '\t')}
                                </Highlight>
                            </p>
                            <br/>
                        </div>
                    </div>
                </div>
            );
        } else {
            return (
                <div className="container">
                    <div className="jumbotron">
                        <button type="button" className="btn btn-primary pull-right" onClick={this.handleEdit}>
                            <img src="./Images/left-arrow.png" height="17" width="17"/> &nbsp;Back
                        </button>
                        <br/>
                        <h4>The following results were generated during the test.</h4>
                        <b>Test Result:</b>{imp_result}
                        <b>Profile:</b> {sessionStorage.proType}<br/><b>Scope:</b> {sessionStorage.scope}
                        <br/><b>Redirect URI:</b> {sessionStorage.callBack}<br/><b>Authorization
                        Endpoint:</b> {sessionStorage.authEndpoint}
                    </div>
                    <br/>

                    <div className="panel panel-primary">
                        <div className="panel-heading">Authorization</div>
                        <div className="panel-body"><br/>
                            <h5>Authorization Request</h5><p>
                                <Highlight className='javascript'>
                                    {sessionStorage.requestUrl}
                                </Highlight></p>
                            <br/>
                            <h5>Authorization Response</h5><p>
                                <Highlight className='javascript'>
                                    {this.state.responseUrl}
                                </Highlight></p>
                        </div>
                    </div>
                    <div className="panel panel-primary">
                        <div className="panel-heading">Tokens Received</div>
                        <div className="panel-body"><br/>
                            <h5>ID Token (Encoded)</h5><p>
                                <Highlight className='javascript'>
                                    {this.state.implicit_id}
                                </Highlight></p>
                            <br/>
                            <h5>ID Token Header(Decoded)</h5><p>
                                <Highlight className='javascript'>
                                    {this.state.implicit_header}
                                </Highlight></p>
                            <br/>
                            <h5>ID Token Payload(Decoded)</h5><p>
                                <Highlight className='javascript'>
                                    {JSON.stringify(implicit_body, null, '\t')}
                                </Highlight></p>
                            <br/>
                            <h5>Access Token</h5><p>
                                <Highlight className='javascript'>
                                    {this.state.access_token}
                                </Highlight></p><br/>
                        </div>
                    </div>
                </div>
            );
        }
    }
}
