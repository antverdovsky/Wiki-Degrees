# WikiDegrees
WikiDegrees is a simple calculator for computing the path between any two Wikipedia articles. Given starting and ending articles, WikiDegrees computes the shortest path to reach the ending article by only clicking on the embedded links of the current article, starting from the starting article. This project is similar to, and is inspired by, the [Oracle of Bacon](https://oracleofbacon.org/) project.

## Installation
Either fork or clone the repository and compile it using your preferred Java compiler. Note that WikiDegrees uses JSON data to compute the path and so the GSON API is required to run the program. A GSON exernal jar file can be acquired for [here](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.6.2/).

## Usage
To open the calculator, simply run it from your command line terminal or IDE. WikiDegrees allows the use of the following command line arguments:
```
-t | Displays the time taken to compute the path.
-d | Displays useful debug information while the path is being computed.
-h | Displays the help for the program.
```
Once the program opens, you will prompted for the names of the starting and ending articles. Either enter the name of each article, or use ```%r``` to fetch a random article name. Once both article names are entered, the calculator will compute the path and display it when completed. The names of the links which can be navigated in order to trace the path will be displayed. On occasion, a link may be embedded in a Wikipedia article under a different name. If this occurs, the embedded name will be surrounded by brackets.
### Example
To calculate the path between a random article and GitHub, we can run the program with the following inputs:
```
Enter starting article name: %r
Enter ending article name: GitHub
```
After the path is computed, we may get a path like this:
```
Searching for path between "Lembarg" and "GitHub"
Degrees of Separation: 4
Path: 
	Lembarg -> 
	Voivodeships of Poland [Voivodeship] -> 
	Encyclopædia Britannica -> 
	IPhone -> 
	GitHub
```
In this case, the random article ```Lembarg``` was chosen, and the path between it and GitHub was computed. The path can now be traced by navigating from each article in the path to the next. Note that the ```Lembarg``` does not contain the article ```Voivodeships of Poland```, but rather just ```Voivodeship```, as indicated by the brackets. Note that this is just one possible path of many. While all paths are always the shortest possible, there may be different paths of the same number of degrees of separation.
## Remarks
The calculator works on the live version of Wikipedia articles and therefore an active internet connection is required in order to compute the path. A high speed internet connection is strongly recommended since the majority of the path computation time is spent fetching data from Wikipedia. This project is not associated with Wikipedia or the Wikimedia Commons repository in any way. Please do not use this project to cheat on The Wiki Game :-).
