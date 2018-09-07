---
title: Algorithm description
layout: blog
---

The algorithm that we used is the one discovered by Élise Janvresse, Thierry de la Rue and Yvan Velenik in ["A note on Domino Shuffling"](http://www.combinatorics.org/ojs/index.php/eljc/article/view/v13i1r30),
in the Electronic Journal of Combinatorics **13** (2006).

We give the main ideas and a brief summary of the algorithm, but we strongly encourage the interested reader to directly check the paper.


#### Probability distribution on the set of tilings

The algorithm allows to generate random tilings of Aztec Diamonds equipped with any weight-based probability distribution. A weight-based probability distribution is defined by assigning a weight to each possible domino.
As usual, the weight of a tiling is then given by the product of all the weights of the dominoes of that
particular tiling.


The partition function is the sum over all possible tilings of their weights. The
probability of one tiling is then its weight divided by the partition function.


#### Main idea of the algorithm


The algorithm was a generalization of an other algorithm introduced by James Propp
in ["Generalized domino-shuffling"](https://arxiv.org/abs/math/0111034) in Theoret.
Comput. Sci., **303** (2-3), 267-301 (2003).


The improvement of the new algorithm is that it allows weights on dominoes to be 0, and not only positive real numbers. This may seem anecdotal at first
sight, but it is that particular feature which enables the generation of other shapes.



The algorithm constructs the Aztec Diamond of order n by starting with a diamond of order 1. Then, given a diamond of order k, it creates a diamond of order k+1, by moving or destroying some dominoes of the diamond of order k, then filling the holes in the diamond of order k+1. The particular geometry of the Aztec Diamond makes
it so that the hole in the diamond of order k+1 is a disjoint union of 2 by 2 squares. Each of them can thus be filled independently by either two horizontal dominoes, or vertical dominoes.


#### First phase: computation of the weights

In order to get a diamond of order n, we need a diamond of order n-1 that was generated according to some probability distribution. This probability distribution obviously has to depend on the probability distribution
of the Aztec Diamond of order n.

Given the weights of order n, the first phase of the algorithm consists in
computing all the weights for order 1, 2, ..., n. These weights are designed in such a way that if a diamond of order k is generated following the weights of order k, then the procedure of the second phase will generate a diamond of order k+1 following the weights of order k+1.


#### Second phase: Diamond construction

Once all the weights are computed, we start by generating a diamond of order 1. Since the only probability distributions are Bernoulli's, it is done easily. Then, once we computed the diamond of order k, we need to move and destroy some of the dominoes. This gives a partially filled Aztec Diamond of order k+1. This first procedure is deterministic.


The hole left in the diamond of order k+1 is now a union of disjoint 2 by 2 squares. For each of these, either two horizontal or vertical dominoes have to be filled at random, according to the weights of the squares at their position.


#### Other shapes

As mentioned, one very important feature of the algorithm is that it allows for weight to be 0, and not only positive real numbers. This feature has two interesting consequences.


The first one is that if you have a shape T and an Aztec diamond shape D such that both T and D\T are tileable, then you can put 0 weights at the boundary between
T and D\T and the induced probabilities of the two shapes are then independent. In order to generate a random tiling of T, you then simply need to generate a random tiling of the embedding Aztec Diamond and take the part inside T.


The second consequence is that if you have a tiling probability of some other kind of shapes, but you are able to put it in bijection with a weight-based probability distribution of dominoes, then you are able to generate tilings of this other kind of shapes. This allows, in particular, to generate lozenge tilings.


#### Performance note

On the implementation side, the algorithm is very well presented in the paper in that all operations are done on "Faces", and operations linked to two different Faces are completely independent, allowing to easily parallelise the implementation.


A more technical aspect is that computing all the weights for diamonds of order 1 to n is memory heavy. This implies the possibility of Out of Memory exceptions. An option allows to get rid of out of memory problems, at the expense of a huge performance loss.


#### Counting tilings and computing partition functions

Another nice consequence of the algorithm is that it allows to easily compute the number of tilings of shapes. All you need to do is to take one particular tiling of your shape embedded in an Aztec Diamond, define the weights as described in the "Other Shapes" subsection, and compute the probability to see that particular tiling. Since all tilings are equi-probable, you directly get the number of tilings.


More precisely, you get the probability of a tiling T by knowing the probability of the pre-images of T, and the probability that these pre-images generate T. By recurrence, you can easily let your computer compute these.


The two difficulties are that you need to know explicitly the number of tilings the complement of your shape, and the second is that, in order to simplify the computations, you need to find a particular tiling T that has the least pre-images, otherwise you will run into Out Of Memory issues. The first difficulty can be easily lifted by adding extras 0 weights in the complementary, so that the number of tilings is 1.


In principle, this technique could be used to find explicit and closed formulas for computing the number of tilings of shapes. In practice however, this will lead to difficult computations that will not be that easy to simplify.


#### Why four colours?

The tilings of the Aztec Diamonds are usually painted with four colours, while there are only two types of dominoes. The reason for that comes from the algorithm, and in particular from Phase 2. When going from one diamond to the next one, we first move dominoes according to some rule: some vertical dominoes go one square to
the right (east), while the others one square to the left (west). Similarly, some horizontal dominoes go one square to the top (north), and the others one square to the bottom (south). (An additional rule states that if, doing so, two dominoes exchange their positions, they are destroyed instead.)


In the drawings, north, south, west and east dominoes are painted respectively in red, blue, yellow and green. Rigorously, the colouring of the dominoes depends on the parity of abscissa of the left square of horizontal dominoes, and the parity of the ordinate of the bottom square of vertical dominoes.



If one sees the growth of a frozen corner as a random process when going from diamond of order n to n+1, then it can be put in bijection with the Totally asymmetric
simple exclusion process.
