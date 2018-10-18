package com.example.travis.ichmfapp.preprocessor.tree;

import com.example.travis.ichmfapp.symbollib.RecognizedSymbol;

import java.util.ArrayList;



public class Node {
    private char symbolChar;
    private RecognizedSymbol symbol;

    private Boolean fraction;


    /**
     *      @param parent each node will have a parent.
     *      @param child Each node can only have one child ,
     *              only the child node will contain information of its siblings
     *              (first symbol in the sibling)
     *
     */
    private Node parent;
    private Node child;
    //only the first child will store siblings
    private ArrayList<Node> siblings;
    private int symID;


    //in respect of parent node to this node
    private String realation;

    public Node(){}
    public Node (RecognizedSymbol Symbol, Node Parent, String Bond){
        this.parent = Parent;
        this.symbol = Symbol;
        this.symID = symbol.getId();
        this.symbolChar = Symbol.getSymbolChar();
    }


    public Node getParent() {return this.parent;}
    public Node getChild() {return this.child;}
    public int getID(){return this.symID;}
    public RecognizedSymbol getSymbol(){return this.symbol;}
    public ArrayList<Node> getSiblings() {return this.siblings;}

    public ArrayList<RecognizedSymbol> getSiblingSymbols(){

        ArrayList<RecognizedSymbol> list = new ArrayList<RecognizedSymbol>();
        for (int i =0; i< siblings.size();i++) {
            list.add(siblings.get(i).getSymbol());
        }
        return list;
    }

    public void setParent(Node Parent, String bond){
        this.parent = Parent;
        this.realation = bond;
    }
    public void setChild(Node Child){this.child = Child;}

    public void addSibling(Node Sibling){
        this.siblings.add(Sibling);
    }

    public void setSiblings(Node Sibling){
        this.siblings.clear();
        this.siblings.add(Sibling);}


}
