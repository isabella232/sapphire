### Introduction

[Sapphire](http://www.eclipse.org/sapphire/) is a user interface development framework that improves productivity.
Instead of focusing on individual widgets, layouts and data binding, the developers
focus on modeling the semantics of the data and declaring the general intent of
how the data it to be presented.

[More Information](http://www.eclipse.org/sapphire/documentation/introduction)
 
### License

[Eclipse Public License (EPL)](http://www.eclipse.org/legal/epl-v10.html)

### Releases

Information on past and future releases along with the downloads can be found on 
[the releases page](http://www.eclipse.org/sapphire/releases/).
 
### Discussion
 
Questions should be directed to [the adopter forum](http://www.eclipse.org/forums/index.php/f/192/).

### Bugzilla
 
This project uses [Bugzilla](https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_status&y_axis_field=bug_severity&z_axis_field=&no_redirect=1&query_format=report-table&short_desc_type=allwordssubstr&short_desc=&classification=Technology&product=Sapphire&resolution=---&resolution=FIXED&longdesc_type=allwordssubstr&longdesc=&bug_file_loc_type=allwordssubstr&bug_file_loc=&status_whiteboard_type=allwordssubstr&status_whiteboard=&keywords_type=allwords&keywords=&bug_id=&bug_id_type=anyexact&votes=&votes_type=greaterthaneq&emailtype1=substring&email1=&emailtype2=substring&email2=&emailtype3=substring&email3=&chfieldvalue=&chfieldfrom=&chfieldto=Now&j_top=AND&f1=noop&o1=noop&v1=&format=table&action=wrap)
to track issues and enhancement requests.
 
 * [Search](https://bugs.eclipse.org/bugs/query.cgi?classification=Technology&product=Sapphire)
 * [Report Problems or Request Enhancements](https://bugs.eclipse.org/bugs/enter_bug.cgi?classification=Technology&product=Sapphire)
 
### Installing

Sapphire is distributed as a p2 repository, from which the various components can be installed. Every [release page](http://www.eclipse.org/sapphire/releases/)
either lists the URL of the repository (for finished releases) or includes a link to a Hudson build job (for in-progress releases).

[Releases](http://www.eclipse.org/sapphire/releases/)
 
### Building

The build is easy to run on a local machine.

 1. Make sure that you have JDK 8 and Ant installed. Both should be on the path.
 2. Set `JDK_8_HOME` environment variable to point to your JDK 8 install.
 3. Clone the Sapphire Git repository and pick the desired branch.
 4. Open a shell to the Git workspace and execute `ant dev-build`.

If you are working with multiple Sapphire branches, you may want to share the bundle pool so that every 
branch does not need to re-download everything it needs. The default bundle pool folder is 
`[root]/releng/pool`, but you can change it by setting the `SAPPHIRE_BUNDLE_POOL` environment
variable or the `bundle.pool` property when invoking the build. An absolute path is required in both cases.

Example: `ant dev-build -Dbundle.pool=d:\Sapphire\Pool`

Once the build completes, you will notice the following key folders in the Git workspace:

 * **build/repository** : Repository of build artifacts, including runtime bundles, source bundles and the SDK.
 * **dev-eclipse** : Eclipse with select plugins along with Sapphire SDK from the build. This Eclipse configuration is designed to be appropriate for working on Sapphire.
 * **dev-target** : Eclipse with select plugins along with all of Sapphire, including source bundles. This Eclipse configuration is designed to make a good target platform. 

### Contributing

Contributions to this project are always welcome as a Bugzilla attachment of a patch. This project currently does not
accept contributions through Gerrit or through GitHub pull requests.

Note that before your contribution can be accepted, you need to
complete the [Contributor License Agreement (CLA)](http://www.eclipse.org/legal/CLA.php). 
See [FAQ](https://www.eclipse.org/legal/clafaq.php) for more information.
