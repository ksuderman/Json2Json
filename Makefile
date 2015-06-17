VERSION=$(shell cat VERSION)
BRANCH=$(git branch | grep \* | cut -d\  -f2)

APP=j2j
JAR=target/$(APP)-$(VERSION).jar

help:
	@echo
	@echo "Available goals are:"
	@echo
	@echo "      clean : Clean removes all artifacts from previous builds."
	@echo "        jar : Creates the jar file."
	@echo "   snapshot : Deploys a snapshot to Nexus."
	@echo "     deploy : Deploys a release version to Nexus."
	@echo "       help : Displays this help message."
	@echo
	
jar:
	mvn package
	
clean:
	mvn clean
		
snapshot:
	issnapshot ; if [ $? -eq 1 ] ; then exit 1 ; fi
	mvn javadoc:jar source:jar deploy
	
deploy:
	pom ; if [ $? -eq 1 ] ; then exit 1 ; fi
	mvn javadoc:jar source:jar deploy
	
	
