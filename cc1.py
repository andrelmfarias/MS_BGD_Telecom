import unittest


# Given a string and a non-negative int n, return a larger string
# that is n copies of the original string.

def string_times(string, n):
    return string*n

# Given an array of ints, return True if one of the first 4 elements
# in the array is a 9. The array length may be less than 4.
def array_front9(nums):
    longueur = min(len(nums),4)
    return 9 in nums[:longueur]

# Given a string, return the count of the number of times
# that a substring length 2 appears  in the string and also as
# the last 2 chars of the string, so "hixxxhi" yields 1 (we won't count the end substring).
def last2(string):
    end = string[-2:]
    count = 0
    char1 = string[0]
    for char2 in string[1:-2]:
        if(char1 + char2 == end):
            count = count + 1
        char1 = char2
    return count

#Write a proramm that returna dictionary of occurences of the alphabet for a given string.
# Test it with the Lorem upsuj
#"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
def occurences(text):
    count = {}
    for letter in text:
        if letter in text:
            count[letter] += 1
        else:
            count[letter] = 1
    return count

#Write a program that maps a list of words into a list of
#integers representing the lengths of the correponding words.
def length_words(array):
    length = []
    for word in array:
        length.append(len(word))
    return length


#Write a function that takes a number and returns a list of its digits.
def number2digits(number):
    digits = []
    for n in str(number):
        digits.append(n)
    return digits

#Write function that translates a text to Pig Latin and back.
#English is translated to Pig Latin by taking the first letter of every word,
#moving it to the end of the word and adding 'ay'
def pigLatin(text):
    words = text.split(" ")
    translated_text = ""
    for word in words:
        translated_text = translated_text + word[2:] + word[:2] + " "
    return translated_text


#write fizbuzz programm
def fizbuzz():
    return


response = {
  "nhits": 1000,
  "parameters": {}
  "records": [
    {
      "datasetid": "les-1000-titres-les-plus-reserves-dans-les-bibliotheques-de-pret",
      "recordid": "4b950c1ac5459379633d74ed2ef7f1c7f5cc3a10",
      "fields": {
        "nombre_de_reservations": 1094,
        "url_de_la_fiche_de_l_oeuvre": "https://bibliotheques.paris.fr/Default/doc/SYRACUSE/1009613",
        "url_de_la_fiche_de_l_auteur": "https://bibliotheques.paris.fr/Default/doc/SYRACUSE/1009613",
        "support": "indéterminé",
        "auteur": "Enders, Giulia",
        "titre": "Le charme discret de l'intestin [Texte imprimé] : tout sur un organe mal aimé"
      },
      "record_timestamp": "2017-01-26T11:17:33+00:00"
    },
    {
      "datasetid":"les-1000-titres-les-plus-reserves-dans-les-bibliotheques-de-pret",
      "recordid":"3df76bd20ab5dc902d0c8e5219dbefe9319c5eef",
      "fields":{
        "nombre_de_reservations":746,
        "url_de_la_fiche_de_l_oeuvre":"https://bibliotheques.paris.fr/Default/doc/SYRACUSE/1016593",
        "url_de_la_fiche_de_l_auteur":"https://bibliotheques.paris.fr/Default/doc/SYRACUSE/1016593",
        "support":"Bande dessinée pour adulte",
        "auteur":"Sattouf, Riad",
        "titre":"L'Arabe du futur [Texte imprimé]. 2. Une jeunesse au Moyen-Orient, 1984-1985"
      },
      "record_timestamp":"2017-01-26T11:17:33+00:00"
    },
  ]
}

#Given the above response object extract a array of records with columns nombre_de_reservations , auteur and timestamp
def flatten():
    return



# Here's our "unit tests".
class Lesson1Tests(unittest.TestCase):
    fizbuzz()
    def testArrayFront9(self):
        self.assertEqual(array_front9([1, 2, 9, 3, 4]) , True)
        self.assertEqual(array_front9([1, 2, 3, 4, 9]) , False)
        self.assertEqual(array_front9([1, 2, 3, 4, 5]) , False)

    def testStringTimes(self):
        self.assertEqual(string_times('Hel', 2),'HelHel' )
        self.assertEqual(string_times('Toto', 1),'Toto' )
        self.assertEqual(string_times('P', 4),'PPPP' )

    def testLast2(self):
        self.assertEqual(last2('hixxhi') , 1)
        self.assertEqual(last2('xaxxaxaxx') , 1)
        self.assertEqual(last2('axxxaaxx') , 2)

    def testLengthWord(self):
        self.assertEqual(length_words(['hello','toto']) , [5,4])
        self.assertEqual(length_words(['s','ss','59fk','flkj3']) , [1,2,4,5])

    def testNumber2Digits(self):
        self.assertEqual(number2digits(8849) , [8,8,4,9])
        self.assertEqual(number2digits(4985098) , [4,9,8,5,0,9,8])

    def testPigLatin(self):
        self.assertEqual(pigLatin("The quick brown fox") , "Hetay uickqay rownbay oxfay")



def main():
    unittest.main()

if __name__ == '__main__':
    main()