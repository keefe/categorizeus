# categorizeus

categorize.us is a decentralized communication system based on conversations and categories. Categories are basically tags, corresponding to classification of data. Conversations are a series of messages replying to other messages, starting with some root message. Messages themselves are currently very simple, but will expand to include flexible data sets, so these conversations may occur in structured data - perhaps even supplying evidence based information exchange. 

For the latest (and hopefully last, for a long time) reboot of this system, I took a different approach. Rather than developing in private until it's perfect, I decided to spend approximately one week (40 hours or so), starting from scratch, using the absolute minimal set of libraries. The goal would be to establish the basic conversation framework and deploy the system at the end of the week. Naturally, finding 40 actual hours of spare time where I felt like writing more code took some months to accumulate. This repository is the result of that week experiment. 

Unfortunately, including a client for talking to decentralized peers slipped the week, but refactoring for a serverless deploy made it in.

### Active Development Continues under categorizeus/categorizeus here at [github](https://github.com/categorizeus/categorizeus) 

Run the main application as such:

 mvn exec:java -Dexec.mainClass="us.categorize.App" -Dexec.args=""

In order to initialize the database, run this command

 mvn exec:java -Dexec.mainClass="us.categorize.App" -Dexec.args="initialize"

Visit localhost:8080 to see a basic forum. 