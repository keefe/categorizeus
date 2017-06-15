#testing cloud9 double check
git pull
mvn clean compile
mvn exec:java -Dexec.mainClass="us.categorize.App" -Dexec.args=""
