import sys
import requests
import glob
import os
import json
import re

if len(sys.argv) < 4:
    print 'Missing input parameters', len(sys.argv)
    exit()

TOKEN = sys.argv[1]
USERNAME = sys.argv[2]
PROJECT_NAME = sys.argv[3]

headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-API-Token': TOKEN
}

def get_distribution_groups():
    get_url = 'https://api.appcenter.ms/v0.1/apps/%s/%s/recent_releases' % (USERNAME, PROJECT_NAME)
    return requests.get(url = get_url, headers=headers).json()


releases = get_distribution_groups()
vc = 0

for x in releases:
    if vc < int(x['version']):
        vc = int(x['version'])
vc = vc + 1
sys.stdout.write(str(vc))
sys.stdout.flush()