# Set the root logger to
log4j.rootLogger=info, MongoDB

# MongoDB appender classname
# To log with a PatternLayout, use org.log4mongo.MongoDbPatternLayoutAppender
log4j.appender.MongoDB=org.log4mongo.MongoDbAppender

# MongoDB appender properties
#  All are optional - defaults shown below (except for userName and password, which default to undefined)
#  If using a replica set, set hostname to blank space-delimited list of host seeds. Don't include arbiters.
#      Also, set port to either one port that all hosts will use or space-delimited list of one port per hostname
log4j.appender.MongoDB.hostname=localhost
log4j.appender.MongoDB.port=27017
log4j.appender.MongoDB.databaseName=deployment_app
log4j.appender.MongoDB.collectionName=log
#log4j.appender.MongoDB.userName=open
#log4j.appender.MongoDB.password=sesame

log4j.logger.org.mongodb=ERROR