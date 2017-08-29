import sys

def dataPreProcess(fTrain):

    """
    get the attribute of the dataset to be trained
    get the number of the values the attribute can take

    """
    lines = [line.strip() for line in fTrain.readlines()]

    lines.pop(0)  #pop the first

    num_read_total = 0
    num_constraints_total = 0
    time_total = 0
    i = 0
    for line in lines:
        vec = line.split()
        num_read_total += int(vec[0])
        num_constraints_total += int(vec[1])
        time_total += int(vec[2])

        # i = i + 1
        # if i==8011:
        #     break

#     get the average
    num_read_avg = num_read_total/len(lines)
    num_constraints_avg = num_constraints_total / len(lines)
    time_avg = time_total / len(lines)

    print "----average-----"
    print num_read_avg
    print num_constraints_avg
    print time_avg
    print "----total-------"
    print num_read_total
    print num_constraints_total
    print time_total


def main():
    print "hello"
    f_input = open(sys.argv[1], "r")
    dataPreProcess(f_input)

if __name__ == '__main__':
    main()