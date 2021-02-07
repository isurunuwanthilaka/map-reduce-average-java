import random

fo = open("dataset.txt", "+a")

sum = 0

for i in range(10000):
    i = random.randint(100, 1000)
    sum += i
    fo.write(str(i)+"\n")

fo.close()
print(sum/10000)
