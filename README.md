# Aztec Diamonds

The goal of this project is to allow, in just a few clicks, to generate random tilings of Aztec Diamonds and other various shapes.

An Aztec Diamond is a shape in the plane, composed of 1 by 1 squares. The shape looks like a Diamond. A tiling is a way of covering the shape with 1 by 2 or 2 by 1 dominoes. In 1992, the remarkably simple formula giving the number of all possible tilings of an Aztec Diamond of some order was discovered. The order of the Diamond is the number of rows in the upper half plane.

Aside from counting the number of tilings, taking one tiling at random led to amazingly rich structure that is still studied today. The number of tilings growth exponentially with the size of the shapes, and generating random tilings is thus a challenge.

The [website](https://sites.uclouvain.be/aztecdiamond/) contains a lot of information about Aztec Diamonds, and the possibilities of this project. A [desktop version](https://github.com/sherpal/AztecDiamond/releases) is also available, and allows for much bigger performances than the online version.

## Algorithm

The algorithm implemented was discovered in 2006 by Élise Janvresse, Thierry de la Rue and Yvan Velenik in [A Note on Domino Shuffling](http://www.combinatorics.org/ojs/index.php/eljc/article/view/v13i1r30/pdf).

## Technology

The project is implemented in Scala, and uses [Scala.js](https://www.scala-js.org/) for online version. The desktop version uses [Electron](https://electronjs.org/) for the user interface, and spawns a pure Scala application for computations, offering a performance boost up to 10 times better than the online version.


## Acknowledgments

- the Université catholique de Louvain, for hosting the website.
- Justin Dekeyser, for his enormous help in HTML and CSS,
- Tom Claeys, for his valuable comments and remarks, and for encouraging this project from the beginning,
- Sébastien Doeraene, for his help with Scala and Scala.js in particular
- Nicolas Radu and Alban Jago, for comments and bugs