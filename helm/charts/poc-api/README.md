# Helm starter

## Installing the starter
```bash
rm -Rf ${HOME}/.helm/starters/humanizei-default
git clone https://source.developers.google.com/p/humanizei-alpha/r/poc-api \ 
    ${HOME}/.helm/starters/humanizei-default
```

## Create a new chart using the starter
```bash

CHART_NAME=${PWD##*/}

mkdir -p helm && cd helm
helm create --starter humanizei-default ${CHART_NAME}

find ./${CHART_NAME} -type f -exec sed -i 's|poc-api|'"${CHART_NAME}"'|g' {} \;

# NOTE: Update the README with help text for users. It should explain how to deploy and use your chart.
```
