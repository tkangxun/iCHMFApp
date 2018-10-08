package com.example.travis.ichmfapp.symbollib;

import org.w3c.dom.*;



/**
 * Created by Travis on 9/8/2018.
 */

public class RecognizedSymbol extends Symbol {




        /**
         * Related Expression Tree node.
         */
        protected Node node;
        /**
         * Index, tagged, as position of this symbol in the list.
         */
        protected int id;
        /**
         * The closeness rating of recognition result.
         */
        protected double error;

        /**
         * Default constructor for Recognized Symbol.
         *
         * @param chracter    The symbol chracter.
         * @param _strokeList Related strokes of the symbol.
         * @param error       The error value for recognition.
         */
        public RecognizedSymbol(char chracter, StrokeList _strokeList, double error) {
            super();
            this.setSymbolChar(chracter);
            this.setStrokes(_strokeList);
            this.error = error;
        }

        public RecognizedSymbol(char chracter) {
            super();
            this.setSymbolChar(chracter);
        }

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getError() {
            return error;
        }

        public void setError(double error) {
            this.error = error;
        }

        /**
         * Get the bounding box of recognized symbol.
         *
         * @return The bounding box
         */
        public Box getBox() {
            return this._Strokes.getBoundingBox();
        }

        /**
         * Get total number of strokes realted to
         * recognized symbol.
         *
         * @return The number of stroke.
         */
        public int getNumberOfStrokes() {
            return this._Strokes.size();
        }

        @Override
        public RecognizedSymbol clone() throws CloneNotSupportedException {
            return (RecognizedSymbol) super.clone();
        }
    }

