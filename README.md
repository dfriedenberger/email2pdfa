# email2pdfa
Use case and workflow to convert email to the long term preservation format PDF/A and sign the pdfs

![email2pdfa](https://raw.githubusercontent.com/dfriedenberger/email2pdfa/master/email2pdfa.png)

I've composed wonderful components to solve the problem preserving emails for easy, long term backup.

- JavaMail  - Api for polling email
- PhantomJS - Full web stack for building view of HTML part of email
- iText - PDF library for creating PDF/A documents and signing them



# Quick Start

```
git clone https://github.com/dfriedenberger/email2pdfa
mvn compile
//Create Configuration
//postbox.properties.template => Configuration for email post box
//sign.properties.template => configuration for signing pdf's
mvn exec:java -Dexec.mainClass="de.frittenburger.email2pdfa.ConsoleApp"
```


## 



#Contact
Dirk Friedenberger, Waldaschaff, Germany

Write me (oder Schreibe mir)
projekte@frittenburger.de

https://ImmerArchiv.de ist ein Langzeitarchiv

