#!/usr/bin/python
# Copyright 2010 Google Inc.
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

# Google's Python Class
# http://code.google.com/edu/languages/google-python-class/

import sys
import re

"""Baby Names exercise

Define the extract_names() function below and change main()
to call it.

For writing regex, it's nice to include a copy of the target
text for inspiration.

Here's what the html looks like in the baby.html files:
...
<h3 align="center">Popularity in 1990</h3>
....
<tr align="right"><td>1</td><td>Michael</td><td>Jessica</td>
<tr align="right"><td>2</td><td>Christopher</td><td>Ashley</td>
<tr align="right"><td>3</td><td>Matthew</td><td>Brittany</td>
...

Suggested milestones for incremental development:
 -Extract the year and print it
 -Extract the names and rank numbers and just print them
 -Get the names data into a dict and print it
 -Build the [year, 'name rank', ... ] list and print it
 -Fix main() to use the extract_names list
"""

def extract_names(filename):
    """
    Given a file name for baby.html, returns a list starting with the year string
    followed by the name-rank strings in alphabetical order.
    ['2006', 'Aaliyah 91', Aaron 57', 'Abagail 895', ' ...]
    """
    with open(filename, 'r') as file:
        text = file.read()
    # Searching for the year
    year = re.search(r'(Popularity in )(\d\d\d\d)', text).group(2)

    # finding all rankings and names
    # names[0] == rank, names[1] == male_name, names[2] == female_name
    names_tuples = re.findall(r'<tr align="right"><td>(\d+)</td><td>(\w+)</td><td>(\w+)', text)

    # arranging everything into a dict
    names_dict = {}
    for (rank, male_name, female_name) in names_tuples:
        # verifying if male name is already in the dictionnary and adding it if it is not there
        if (male_name in names_dict.keys()):
            names_dict[male_name] = min(rank, names_dict[male_name])  # picking the lowest ranking
        else:
            names_dict[male_name] = rank
        # doing the same for female names
        if (female_name in names_dict.keys()):
            names_dict[female_name] = min(rank, names_dict[female_name])  # picking the lowest ranking
        else:
            names_dict[female_name] = rank

    names_list = [name + ' ' + str(rank) for name, rank in names_dict.items()]
    names_list.sort()

    return [year] + names_list


def main():
    # This command-line parsing code is provided.
    # Make a list of command line arguments, omitting the [0] element
    # which is the script itself.
    args = sys.argv[1:]

    if not args:
        print 'usage: [--summaryfile] file [file ...]'
        sys.exit(1)

    # Notice the summary flag and remove it from args if it is present.
    summary = False
    if args[0] == '--summaryfile':
        summary = True
        del args[0]

    # +++your code here+++
    # For each filename, get the names, then either print the text output
    # or write it to a summary file
    for filename in args:
        names_list = (extract_names(filename))
        text = '\n'.join(names_list) + '\n'

        if summary:
            with open(filename+'.summary','w') as sum_file:
                sum_file.write(text)
        else:
            print(text)

if __name__ == '__main__':
    main()
