package com.example.travis.ichmfapp.preprocessor.tree;

import java.util.ArrayList;

/**
 * Created by Travis on 16/10/2018.
 */

public class Tree {
    private Boolean matrix;
    private Boolean fraction;
    private Boolean vectors;
    private Node root;
    private ArrayList<Node> base;
    private ArrayList<Node> allChildren;

    public Tree(Node Base){
        this.base.add(Base);
        this.root = new Node();
        this.matrix= false;

        //this.vectors= false;

    }
    public Node getRoot(){return this.root;}
    public ArrayList<Node> getBase() {return this.base;}

    public void addRoot(Node Base){
        Base.setParent(this.root, "ROOT");
        this.base.add(root);

    }

    public Node symbolFindNode (int symbolID){
        for (int i = 0; i<allChildren.size(); i++){
            if(allChildren.get(i).getID() == symbolID){
                return allChildren.get(i);
            }
        }
        return null;
    }

    public void appendTree (Node newnode){
        if (newnode.getParent() == this.root){
            addRoot(newnode);
        }
        allChildren.add(newnode);
    }
}
