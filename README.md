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


To checkout some basic functionality, get a message:

http://localhost:8080/msg/1

to create a new message


 curl -X POST -H 'Content-Type:application/json' -d@message.json http://localhost:8080/msg
 
 For deploying on EC2 free tier, spin up a postgres RDS instance. 
 Startup a new instance and install java 8 and nginx. 
 
 For https, let's encrypt offers a great free solution, so install certbot and configure it with nginx
 https://certbot.eff.org/#ubuntutrusty-nginx
 for me, this is 
 sudo ./certbot-auto certonly --webroot -w /usr/share/nginx/html/ -d categorize.us -d www.categorize.us 
 
 This will verify domain ownership automatically and generate the appropriate certificates. 
 
 Verify the SSL settings using a simple file server, for example add the following to the nginx config:
 
 server {
    listen 443 ssl;
    server_name categorize.us www.categorize.us;
    root /usr/share/nginx/html;
    ssl_certificate /etc/letsencrypt/live/categorize.us/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/categorize.us/privkey.pem;
}

A full example nginx.conf configuring upstream for the java server is included in this directory. 
 
 Remember to create static/files, currently not automated

To ignore the properties file, 
git update-index --assume-unchanged src/main/resources/categorizeus.properties
.gitignore wasn't working properly
