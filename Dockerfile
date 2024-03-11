FROM maven:3.9.6-eclipse-temurin-17

####################################################################################################
# Adding Google Chrome and ChromeDriver like described in https://gist.github.com/varyonic/dea40abcf3dd891d204ef235c6e8dd79?permalink_comment_id=4886347#gistcomment-4886347

RUN apt-get update -y && apt-get install -y gnupg wget unzip --no-install-recommends

RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list

RUN apt-get update -y

# Set up Chromedriver Environment variables and install chrome
ENV CHROMEDRIVER_VERSION 114.0.5735.90
ENV CHROME_VERSION 114.0.5735.90-1
RUN wget --no-verbose -O /tmp/chrome.deb https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_${CHROME_VERSION}_amd64.deb \
  && apt install -y /tmp/chrome.deb --no-install-recommends \
  && rm /tmp/chrome.deb

ENV CHROMEDRIVER_DIR /chromedriver
RUN mkdir $CHROMEDRIVER_DIR

# Download and install Chromedriver
RUN wget -q --continue -P $CHROMEDRIVER_DIR "http://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip"
RUN unzip $CHROMEDRIVER_DIR/chromedriver* -d $CHROMEDRIVER_DIR

# Put Chromedriver into the PATH
ENV PATH $CHROMEDRIVER_DIR:$PATH

ENV DB_URL=${DB_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}

COPY wait-for-it.sh ./
COPY docker-entrypoint.sh ./
RUN chmod +x wait-for-it.sh docker-entrypoint.sh

COPY target/api-0.0.1-SNAPSHOT.jar api.jar

ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["java", "-Dspring.datasource.url=${DB_URL}", "-Dspring.datasource.username=${DB_USER}", "-Dspring.datasource.password=${DB_PASSWORD}", "-jar", "/api.jar" ]