import sys
from xml.dom import minidom
import requests
import json
import glob
import os

if len(sys.argv) < 7:
    print 'Missing input parameters', len(sys.argv)
    exit()


URL = sys.argv[1]
USERNAME = sys.argv[2]
SLACK_CHANNEL_NAME = sys.argv[3]
CI_PROJECT_NAMESPACE = sys.argv[4]
CI_PROJECT_NAME = sys.argv[5]
CI_JOB_ID = sys.argv[6]

report_file = glob.glob('app/build/reports/lint-results-*.xml')[0]
mydoc = minidom.parse(report_file)

items = mydoc.getElementsByTagName('issue')
error_count = len(items)
headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json'
}
if error_count == 0:
    text = "Congratulations, @%s no problems in lint report!" % USERNAME
else:
    text = "@%s Problems found: %d\n" \
           "<https://gitlab.gbksoft.net/%s/%s/-/jobs/%s/artifacts/raw/app/build/reports/%s.html?inline=false|Download report>" \
           % (USERNAME, error_count, CI_PROJECT_NAMESPACE, CI_PROJECT_NAME, CI_JOB_ID, os.path.basename(report_file).split('.')[0])

data = {
    "channel": SLACK_CHANNEL_NAME,
    "username": "webhooks-integration",
    "link_names":1,
    "text": text
}
json_str = json.dumps(data)
requests.post(url = URL, headers = headers, data = json_str)