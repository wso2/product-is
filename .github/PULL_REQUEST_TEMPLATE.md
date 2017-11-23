### Proposed changes in this pull request

[List all changes you want to add here. If you fixed an issue, please
add a reference to that issue as well.]

-

### When should this PR be merged

[Please describe any preconditions that need to be addressed before we
can merge this pull request.]


### Follow up actions

[List any possible follow-up actions here; for instance, testing data
migrations, software that we need to install on staging and production
environments.]

-


### Checklist (for reviewing)

#### General

- [ ] **Is this PR explained thoroughly?** All code changes must be accounted for in the PR description.
- [ ] **Is the PR labeled correctly?**

#### Functionality

- [ ] **Are all requirements met?** Compare implemented functionality with the requirements specification.
- [ ] **Does the UI work as expected?** There should be no Javascript errors in the console; all resources should load. There should be no unexpected errors. Deliberately try to break the feature to find out if there are corner cases that are not handled.

#### Code

- [ ] **Do you fully understand the introduced changes to the code?** If not ask for clarification, it might uncover ways to solve a problem in a more elegant and efficient way.
- [ ] **Does the PR introduce any inefficient database requests?** Use the debug server to check for duplicate requests.
- [ ] **Are all necessary strings marked for translation?** All strings that are exposed to users via the UI must be [marked for translation](https://docs.djangoproject.com/en/1.10/topics/i18n/translation/).

#### Tests

- [ ] **Are there sufficient test cases?** Ensure that all components are tested individually; models, forms, and serializers should be tested in isolation even if a test for a view covers these components.
- [ ] **If this is a bug fix, are tests for the issue in place?**  There must be a test case for the bug to ensure the issue won’t regress. Make sure that the tests break without the new code to fix the issue.
- [ ] **If this is a new feature or a significant change to an existing feature?** has the manual testing spreadsheet been updated with instructions for manual testing?

#### Security

- [ ] **Confirm this PR doesn't commit any keys, passwords, tokens, usernames, or other secrets.**
- [ ] **Are all UI and API inputs run through forms or serializers?**
- [ ] **Are all external inputs validated and sanitized appropriately?**
- [ ] **Does all branching logic have a default case?**
- [ ] **Does this solution handle outliers and edge cases gracefully?**
- [ ] **Are all external communications secured and restricted to SSL?**

#### Documentation

- [ ] **Are changes to the UI documented in the platform docs?** If this PR introduces new platform site functionality or changes existing ones, the changes should be documented.
- [ ] **Are changes to the API documented in the API docs?** If this PR introduces new API functionality or changes existing ones, the changes must be documented.
- [ ] **Are reusable components documented?** If this PR introduces components that are relevant to other developers (for instance a mixin for a view or a generic form) they should be documented in the Wiki.
