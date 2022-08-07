#  Identity Server Release Process

## How to keep track of issues for a specific release in github?
* Create a new github milestone to keep track of all the issues being fixed for a specific release. e.g. M1, alpha, beta, rc1.
* Milestone should be closed when released.

## How to update versions
* Following command can be used to update identity components authenticators and few other components.

`mvn versions:update-properties -DgenerateBackupPoms=false -Dincludes=org.wso2.carbon.identity.*,org.wso2.carbon.extension.identity.*,org.wso2.charon,org.apache.rampart.wso2,org.apache.ws.security.wso2,org.wso2.carbon.consent.*,org.wso2.carbon.database.utils -U`

* Need to update charon , balana, rampart, wss4j and sso-agent separately.
* These components are packed into the product-is by:
   
   charon       : [identity-inbound-provisioning-scim2](https://github.com/wso2-extensions/identity-inbound-provisioning-scim2/)
   
   balana       : [carbon-identity-framework](https://github.com/wso2/carbon-identity-framework)
   
   rampart      : [identity-inbound-auth-sts](https://github.com/wso2-extensions/identity-inbound-auth-sts/)
   
   wss4j        : [carbon-identity-framework](https://github.com/wso2/carbon-identity-framework)
   
   sso-agent    : [carbon-deployment](https://github.com/wso2/carbon-deployment/)
   
* Also double check CEP, BPS updates as well.

## How to release in github?
* Create a new release draft on the tag created by a successful Jenkins release.
* Download the distributions from Jenkins and upload them to release draft.
* Publish the release draft.
* Make the public announcement (@iam-dev).

### Public announcement guidelines for milestone releases

The following Email template is used to announce Identity Server milestone releases to the following groups.

*   [iam-dev@wso2.org](mailto:iam-dev@wso2.org)

And a blind carbon copy of the same announcement email is sent to the following group.

*  [iam-engineering-group@wso2.com](mailto:iam-engineering-group@wso2.com)
*  [engineering-group@wso2.com](mailto:engineering-group@wso2.com)

#### Placeholders

<table>
  <tr>
   <td><strong>Placeholder</strong>
   </td>
   <td><strong>Replaced With</strong>
   </td>
   <td><strong>Example</strong>
   </td>
  </tr>
  <tr>
   <td><i>RELEASED_VERSION</i>
   </td>
   <td><i>digit-1</i>.<i>digit-2</i>.<i>digit-3</i> M <i>milestone</i>
   </td>
   <td>5.11.0 M4
   </td>
  </tr>
  <tr>
   <td><i>IS_ARTIFACT_LOCATION</i>
   </td>
   <td>The download location of the released IS distribution for the milestone
   </td>
   <td><a href="https://github.com/wso2/product-is/releases/download/v5.11.0-m4/wso2is-5.11.0-m4.zip">https://github.com/wso2/product-is/releases/download/v5.11.0-m4/wso2is-5.11.0-m4.zip</a>
   </td>
  </tr>
  <tr>
   </td>
  </tr>
  <tr>
   <td><i>GITHUB_MILESTONE<i>
   </td>
   <td>The URL to the closed milestone in the GitHub
   </td>
   <td><a href="https://github.com/wso2/product-is/milestone/133?closed=1">https://github.com/wso2/product-is/milestone/133?closed=1</a>
   </td>
  </tr>
</table>

The following email template uses the above placeholders, and they should be populated according to the milestone 
release.

---

## WSO2 Identity and Access Management team is pleased to announce the release of Identity Server <code><em><RELEASED_VERSION></em></code>!

## **Download**

You can download WSO2 Identity Server <code><em><RELEASED_VERSION> </em></code> from 
<code><em><IS_ARTIFACT_LOCATION></em></code>.

## **How to run**

1. Extract the downloaded zip file.
2. Go to the _bin_ directory in the extracted folder.
3. Run the _wso2server.sh_ file if you are on a Linux/Mac OS or run the _wso2server.bat_ file if you are on a Windows OS.
4. Optionally, if you need to start the OSGi console with the server, use the _-DosgiConsole_ property when starting the server.

## What's new in WSO2 Identity Server <code><em><RELEASED_VERSION></em></code></strong>

A list of all the new features and bug fixes shipped with this release can be found[ ](https://github.com/wso2/product-is/milestone/96?closed=1)<code><em><GITHUB_MILESTONE></em></code>

## **Known Issues**

All the open issues pertaining to WSO2 Identity Server are reported at the following location:

*   [IS Runtime](https://github.com/wso2/product-is/issues)

## **Contribute to WSO2 Identity Server**

### **Mailing Lists**

Join our mailing lists and correspond with the developers directly. We also encourage you to take part in discussions related to the product in the architecture mailing list. If you have any questions regarding the product you can use our StackOverflow forum to raise them as well.

*   Developer List: iam-dev@wso2.org
*   User Forum: [StackOverflow](http://stackoverflow.com/questions/tagged/wso2is)

## **Slack Channels**

Join with us via our [wso2is.slack.com](https://join.slack.com/t/wso2is/shared_invite/enQtNzk0MTI1OTg5NjM1LTllODZiMTYzMmY0YzljYjdhZGExZWVkZDUxOWVjZDJkZGIzNTE1NDllYWFhM2MyOGFjMDlkYzJjODJhOWQ4YjE) for even better communication. You can 
talk to our developers directly regarding any issues, concerns about the product. We encourage you to start 
discussions, or join any on going discussions with the team, via our slack channels.

* Discussions about developments: [Dev Channel](https://wso2is.slack.com/messages/dev)
* New releases: [Release Announcement Channel](https://wso2is.slack.com/messages/releases)

### **Reporting Issues**

We encourage you to report issues, improvements, and feature requests regarding WSO2 Identity Server through our public[ WSO2 Identity Server GIT Issues](https://github.com/wso2/product-is/issues).

<b>Important</b>: Please be advised that security issues must be reported to security@wso2.com, not as GitHub issues,in order to reach the proper audience. We strongly advise following the [WSO2 Security Vulnerability Reporting 
 Guidelines](https://docs.wso2.com/display/Security/WSO2+Security+Vulnerability+Reporting+Guidelines) when reporting the security issues.

For more information about WSO2 Identity Server, please see[ https://wso2.com/identity-and-access-management](https://wso2.com/identity-and-access-management) or visit the[ WSO2 Oxygen Tank](http://wso2.com/library/) developer portal for additional resources.

`~` The WSO2 Identity and Access Management Team `~`

---

## TODO’s before performing Alpha release
* All the major features shipped with the target release should be completed by alpha release.

## TODO’s after performing the Alpha release
* Inform the installation experience team about the installers. 

## TODO’s before performing Beta release
* Make sure there are no issues affecting the target release with type: bug and priority: highest/high states (All L1/L2 issues should be resolved).

## TODO’s before performing RC release
* Complete the security scan reports (Veracode and Qualys) and get the approval from security team.
* Update the release_note.html, README.txt and other files inside the product distribution to the latest versions.

## TODO's after performing the RC release (Vote period) 
* Inform marketing team to get ready on carry out public announcements and other post release tasks on the release.

## How to perform Release Candidate (RC) releases in Jenkins?
* Release version should be the final GA release version
* Development version should be the same as earlier releases
* Custom SCM tag should include the relevant candidate version. E.g. rc1
* Make sure to un-tick the ‘close Nexus Staging Repository’ option
* If the vote passes for the candidate we can manually close and release the product in Nexus. Else we can drop the candidate.
* Follow the same procedure to publish release of RC on github as other releases.
* Make sure to rename the downloaded pack to rc before uploading to github. E.g. for RC1 release pack should be renamed to <product-name><version>-rc1.zip.
* Announce for a vote on the release candidate @architecture+dev.

## How to convert RC to GA release when vote passes?
* Login to nexus and go to staging repositories. https://maven.wso2.org/nexus/#stagingRepositories
* Make sure other unqualified candidates are dropped.
* Find the corresponding qualified RC release candidate. This should be in open state.
* Close it and release the product

## Post release tasks to be done by product team
* Add product to product-dist > inform marketing teams.
* Sync product to atuwa > inform infra team.
* Add the features to feature-repo (P2-profile).
* Update the development versions to next iteration version.
* Cloud-formation scripts.
* Create a new git tag with the released version removing the RC part from the RC tag. (Ex v5.9.0-rc2 -> v5.9.0)

## Post release tasks to be done by marketing team
* Public announcement
* Update the release matrix
* Sign the pack
* Posting product on WSO2 site
* Adding product to WUM

