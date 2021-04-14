# Ancient Roman Firefighters
https://en.wikipedia.org/wiki/History_of_firefighting

https://en.wikipedia.org/wiki/Vigiles
## Prompt
You have been tasked with organizing a firefighting force for Ancient Rome known as the Vigiles
Urbani or “watchmen of the city”.
Optimize for overall distance traveled among all firefighters.

### Given
- City
  - The City is organized in a grid of squares where each square represents a Building and roads
run between all of them. The size of the city can vary and the coordinates are 0-indexed. It
knows its own dimensions and contains references to all buildings as well as FireDispatch.
- CityNode
  - CityNode is a class that allows us to reference a location in the city.
- Building
  - A Building knows its location, information about whether or not it’s burning, and can toggle its
own burning status.
- FireStation
  - The FireStation is a special building. It is fireproof and has a random location in the city.
### To Do
- FireDispatch
  - FireDispatch is constructed with a reference to its city. It is responsible for initializing and
dispatching the firefighters.
- Firefighter
  - A Firefighter knows their own location and how far they have traveled throughout their lifetime.
In order for a fire to be put out, a Firefighter must be present.
