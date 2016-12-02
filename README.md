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
vi default.properties <= Configure properties
mvn run poll default  <= polling emails
mvn run parse <= parse and extract emails       
mvn run convert <= create screenshots from html parts 
vi sign.properties <= Configure properties
mvn run create sign <= create pdf/a files and sign them
```


## 



#Contact
Dirk Friedenberger, Waldaschaff, Germany

Write me (oder Schreibe mir)
projekte@frittenburger.de

https://ImmerArchiv.de ist ein Langzeitarchiv

