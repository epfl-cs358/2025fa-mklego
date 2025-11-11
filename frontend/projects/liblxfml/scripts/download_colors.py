
#
# This script is used to downloads the colors from https://rebrickable.com/colors/
# If you want to use it to get all the colors, download the html page manually and
# store it as colors.html
#
# This script was only used once by our team to gather the information,
# Please never use it in bulk since that data is static.
#

from bs4 import BeautifulSoup

import csv

with open("colors.html", "r") as file:
    content = file.read()

soup = BeautifulSoup( content, features="html.parser" )

objects = soup.find_all("tr", attrs={ "role": "row" })

with open("colors.csv", "w", newline="") as file:
    writer = csv.writer(file)

    for obj in objects[1:]:
        image, uuid, title, rgb, num_parts, \
            num_sets, first_year, last_year, \
            lego, ldraw, bricklink, brickowl = obj.find_all("td")
        
        row = (
            uuid.get_text().strip(),
            title.get_text().strip(),
            rgb.get_text().strip(),
            lego.get_text().strip().split(" ")[0]
        )

        if "" in row:
            print("INVALID ROW:", row)
        else:
            writer.writerow(row)