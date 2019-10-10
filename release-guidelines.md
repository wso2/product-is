#  Identity Server Release Process

## How to keep track of issues for a specific release in github?
* Create a new github milestone to keep track of all the issues being fixed for a specific release. e.g. M1, alpha, beta, rc1.
* Milestone should be closed when released.

## How to update versions
* Following command can be used to update identity components authenticators and few other components.

`mvn versions:update-properties -DgenerateBackupPoms=false -Dincludes=org.wso2.carbon.identity.*,org.wso2.carbon.extension.identity.*,org.wso2.charon,org.apache.rampart.wso2,org.apache.ws.security.wso2,org.wso2.carbon.consent.*,org.wso2.carbon.database.utils -U`

* Need to update charon , balana, rampart, wss4j and sso-agent seperately.
* Also double check CEP, BPS updates as well.

## How to release in github?
* Create a new release draft on the tag created by a successful Jenkins release.
* Download the distributions from Jenkins and upload them to release draft.
* Publish the release draft.
* Make the public announcement (@architecture + dev).

## TODO’s before performing Alpha release
* All the major features shipped with the target release should be completed by alpha release.

## TODO’s after performing the Alpha release
* Inform the installation experiance team about the installers. 

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
* Make sure to untick the ‘close Nexus Staging Repository’ option
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

