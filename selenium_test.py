from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.common.keys import Keys

# PROXY = "http://172.17.0.17:9090"
PROXY = "http://zap.svc:9090"

#url = 'http://selenium-hub-tuesday3001.52.56.167.35.nip.io/wd/hub'
url = 'http://selenium-hub.tuesday3001.svc:4444/wd/hub'

webdriver.DesiredCapabilities.CHROME['proxy'] = {
    "httpProxy":PROXY,
    "ftpProxy":PROXY,
    "sslProxy":PROXY,
    "noProxy":None,
    "proxyType":"MANUAL",
    "class":"org.openqa.selenium.Proxy",
    "autodetect":False
}


driver = webdriver.Remote(command_executor=url,
                          desired_capabilities=webdriver.DesiredCapabilities.CHROME)
try:
    driver.get("http://juice-shop:3000")
    print("get sucessful")
finally:
    driver.close()
