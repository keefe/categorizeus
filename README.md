# categorizeus


Install postgres locally, in ubuntu just

 sudo apt-get install postgresql postgresql-contrib pgadmin3
sudo -u postgres psql postgres
create a new user
create user categorizeus with password 'password';
create database categories;
GRANT ALL PRIVILEGES ON DATABASE categories to categorizeus;

Setup environment variables so that the server knows where assets are
for example, here is my ~/.bashrc (which also has a couple convenience alias)

alias mvn="~/maven/bin/mvn"
alias mvnecl="~/maven/bin/mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true"
export CATEGORIZEUS_STATIC="/home/keefe/categorizeus/static"
export CATEGORIZEUS_PORT=8080
export CATEGORIZEUS_DB_USER="categorizeus"
export CATEGORIZEUS_DB_PASS="password"
export CATEGORIZEUS_DB="categories"


Run the main application as such:

 mvn exec:java -Dexec.mainClass="us.categorize.App" -Dexec.args=""

In order to initialize the database, run this command

 mvn exec:java -Dexec.mainClass="us.categorize.App" -Dexec.args="initialize"

