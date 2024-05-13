from faker import Faker
import random

fake = Faker()
Faker.seed(random.seed())

file = open("owners_table.csv", "a")

for _ in range(100000):
    name = fake.name()
    address = fake.street_address()
    randLine = open('zip.csv').read().splitlines()
    cityStateZip = random.choice(randLine)
    file.write(name + ',' + address + ',' + cityStateZip + '\n')
    # print(str(id) + "," + name + ',' + address + ',' + cityStateZip)

print("Done")
file.close()