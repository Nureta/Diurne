gradle build
scp ./app/build/distributions/app.zip botmaster@23.94.2.173:~
ssh botmaster@23.94.2.173 "~/update.sh"
