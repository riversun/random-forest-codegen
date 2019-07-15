# Overview

A tool to make machine learning results by the random forest algorithm into standalone source code.

This tool can generate pure Java source code from RandomForest tree model generated by [Weka](https://www.cs.waikato.ac.nz/ml/weka/) or RandomForest Learner.

- Stand-alone prediction is possible.
- In other words, it is possible to predict using a random forest without the need for an external library.

It is licensed under [MIT](https://opensource.org/licenses/MIT).

# How to use

## from Command Line

1.Download jar file from [here](https://github.com/riversun/random-forest-codegen/raw/master/released/rfcodegen-1.0.0.jar)

2.Run from command-line

```shell
java -jar rfcodegen-1.0.0.jar --package com.example --class RandomForest --main --inFile c:/temp/model.txt --outDir c:/temp
```

```
usage: options
 -c,--class <className>       class name of generated source code
 -f,--inFile <treeFile>       tree model file path
 -l,--language <language>     language('java' only)
 -m,--main                    generate main method
 -o,--outDir <outDir>         output directory for generated source code
 -p,--package <packageName>   package name of generated source code
```

## from Your code

1.Add dependency into POM.xml or build.gradle

2.Write your java code

```java
RandomForestCodeGen o = new RandomForestCodeGen.Builder()
        .packageName("org.example")//package name
        .className("RandomForestExample")//class name
        .mainMethod(true)//true:generate main method
        .targetLanguage("java")//target language(java only)
        .tree(rfTreeModel)//String data of tree model
        // .treeFile("model.txt")//tree model file
        .build();

o.generateFile(new File("c:/temp"));
```

# What is 'Model file'

Random Forest Model like as follows.
You can generate it from Weka or RandomForest Learner.

```txt
RandomTree
==========

petallength < 2.45 : Iris-setosa (48/0)
petallength >= 2.45
|   petalwidth < 1.65
|   |   petalwidth < 1.45 : Iris-versicolor (41/0)
|   |   petalwidth >= 1.45
|   |   |   sepallength < 7.05
|   |   |   |   petallength < 5 : Iris-versicolor (12/0)
|   |   |   |   petallength >= 5
|   |   |   |   |   sepallength < 6.15 : Iris-versicolor (1/0)
|   |   |   |   |   sepallength >= 6.15 : Iris-virginica (2/0)
|   |   |   sepallength >= 7.05 : Iris-virginica (1/0)
|   petalwidth >= 1.65 : Iris-virginica (45/0)

Size of the tree : 13


RandomTree
==========

petallength < 2.7 : Iris-setosa (55/0)
petallength >= 2.7
|   petallength < 4.85
|   |   petalwidth < 1.65 : Iris-versicolor (45/0)
|   |   petalwidth >= 1.65
|   |   |   sepallength < 5.4 : Iris-virginica (1/0)
|   |   |   sepallength >= 5.4 : Iris-versicolor (1/0)
|   petallength >= 4.85
|   |   petalwidth < 1.7
|   |   |   sepalwidth < 2.65 : Iris-virginica (2/0)
|   |   |   sepalwidth >= 2.65
|   |   |   |   petallength < 5.45 : Iris-versicolor (4/0)
|   |   |   |   petallength >= 5.45 : Iris-virginica (1/0)
|   |   petalwidth >= 1.7 : Iris-virginica (41/0)

Size of the tree : 15

=== End of trees ===

=== Begin of sample ===
sepallength,sepalwidth,petallength,petalwidth,class
5.1,3.5,1.4,0.2,Iris-setosa
4
=== End of sample ===
```

# Example of Generated Source code

The generated source code is shown below.
In this way, a prediction engine using a random forest is generated as one class file.
 (Comments are omitted, indents have been adjusted.)

This is a generated classifier that trained the type of iris.

```Java

package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomForestExample {

    public static void main(String[] args) throws Exception {

        RandomForestExample rf = new RandomForestExample();

        // Set attribute values for prediction
        rf.sepallength = 5.1;
        rf.sepalwidth = 3.5;
        rf.petallength = 1.4;
        rf.petalwidth = 0.2;

        // Perform 'class' prediction
        Prediction prediction = rf.runClassification();

        System.out.println("prediction=" + prediction);

    }

    public double sepallength;
    public double petalwidth;
    public double sepalwidth;
    public double petallength;

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("MyClass: ");
        b.append(MyClass);
        b.append(", sepallength: ");
        b.append(sepallength);
        b.append(", petalwidth: ");
        b.append(petalwidth);
        b.append(", sepalwidth: ");
        b.append(sepalwidth);
        b.append(", petallength: ");
        b.append(petallength);
        return b.toString();
    }

    protected void runClassifiers(List<Prediction> predictions) {
        predictions.add(runTree0());
        predictions.add(runTree1());
    }

    private Prediction runTree0() {
        if (petallength < 2.45) {
            return new Prediction("Iris-setosa", 48, 0);
        }
        if (petallength >= 2.45) {
            if (petalwidth < 1.65) {
                if (petalwidth < 1.45) {
                    return new Prediction("Iris-versicolor", 41, 0);
                }
                if (petalwidth >= 1.45) {
                    if (sepallength >= 7.05) {
                        return new Prediction("Iris-virginica", 1, 0);
                    }
                    if (sepallength < 7.05) {
                        if (petallength < 5) {
                            return new Prediction("Iris-versicolor", 12, 0);
                        }
                        if (petallength >= 5) {
                            if (sepallength >= 6.15) {
                                return new Prediction("Iris-virginica", 2, 0);
                            }
                            if (sepallength < 6.15) {
                                return new Prediction("Iris-versicolor", 1, 0);
                            }
                        }
                    }
                }
            }
            if (petalwidth >= 1.65) {
                return new Prediction("Iris-virginica", 45, 0);
            }
        }
        return null;
    }

    private Prediction runTree1() {
        if (petallength < 2.7) {
            return new Prediction("Iris-setosa", 55, 0);
        }
        if (petallength >= 2.7) {
            if (petallength >= 4.85) {
                if (petalwidth < 1.7) {
                    if (sepalwidth >= 2.65) {
                        if (petallength >= 5.45) {
                            return new Prediction("Iris-virginica", 1, 0);
                        }
                        if (petallength < 5.45) {
                            return new Prediction("Iris-versicolor", 4, 0);
                        }
                    }
                    if (sepalwidth < 2.65) {
                        return new Prediction("Iris-virginica", 2, 0);
                    }
                }
                if (petalwidth >= 1.7) {
                    return new Prediction("Iris-virginica", 41, 0);
                }
            }
            if (petallength < 4.85) {
                if (petalwidth < 1.65) {
                    return new Prediction("Iris-versicolor", 45, 0);
                }
                if (petalwidth >= 1.65) {
                    if (sepallength < 5.4) {
                        return new Prediction("Iris-virginica", 1, 0);
                    }
                    if (sepallength >= 5.4) {
                        return new Prediction("Iris-versicolor", 1, 0);
                    }
                }
            }
        }
        return null;
    }

    /**
     * This class implements the basic structure of the Random Forests. It contains methods to run the classification.
     *
     * Modified:
     * author Tom Misawa(riversun.org@gmail.com)
     * version July 10, 2019
     * Copyright: MIT Licence
     *
     * Original:
     * author Martin Pielot
     * version June 30, 2015
     * Copyright: MIT Licence
     */
    public String MyClass;

    public final boolean use_votes = true;

    public Prediction runClassification() {

        List<Prediction> predictions = new ArrayList<Prediction>();
        runClassifiers(predictions);
        Map<String, Integer> winners = new HashMap<String, Integer>();
        Map<String, Integer> votes = new HashMap<String, Integer>();
        int totalVotes = 0;
        for (Prediction prediction : predictions) {
            if (prediction != null) {
                inc(winners, prediction.label, 1);
                inc(votes, prediction.label, prediction.numPos);
                totalVotes += prediction.getNumLeafs();
            }
        }

        String winner = winner(use_votes ? votes : winners);
        int numPosVotes = votes.get(winner);
        int numNegVotes = totalVotes - numPosVotes;

        Prediction result = new Prediction(winner, numPosVotes, numNegVotes);
        return result;
    }

    private String winner(Map<String, Integer> votes) {
        String winner = "";
        int maxVotes = 0;

        for (String label : votes.keySet()) {
            int voteCount = votes.get(label);
            if (voteCount > maxVotes) {
                winner = label;
                maxVotes = voteCount;
            }
        }

        return winner;
    }

    private void inc(Map<String, Integer> votes, String label, int voteCount) {
        int count = 0;
        if (votes.containsKey(label)) {
            count = votes.get(label);
        }
        votes.put(label, count + voteCount);
    }

    public static class Prediction {

        /** The class label of the prediction, e.g. 'picked' */
        public String label;
        /** The number of votes FOR this label */
        public int numPos;
        /** The number of votes for OTHER labels */
        public int numNeg;
        /** The total number of votes */
        public int total;
        /** Probability - how certain the classifier is that the prediction is correct */
        public double p;

        public Prediction(String label, double numPositive, double numNegative) {
            this.label = label;
            this.numPos = (int) (numPositive + 0.5);
            this.numNeg = (int) (numNegative + 0.5);
            this.total = this.numPos + this.numNeg;
            this.p = getProbability();
        }

        /** Probability - how certain the classifier is that the prediction is correct */
        private double getProbability() {
            if (total == 0)
                return 0;
            int c = 10000;
            double p = c * numPos / total;
            return p / c;
        }

        /** The total number of votes */
        public int getNumLeafs() {
            return total;
        }

        @Override
        public String toString() {
            return "result:" + label + ",  propability: " + p + ",  (positive/negative)=( " + numPos + " / " + numNeg + " )";
        }

    }
}
```

And it's resulted in,

```
prediction=result:Iris-setosa,  propability: 1.0,  (positive/negative)=( 103 / 0 )
```

You can get the prediction result.
(Well, this is only learning data.:p)
