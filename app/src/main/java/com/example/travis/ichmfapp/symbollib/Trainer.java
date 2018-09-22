package com.example.travis.ichmfapp.symbollib;

import com.example.travis.ichmfapp.main.WriteView;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Created by Travis on 13/9/2018.
 */

public class Trainer{

    private SymbolLib objsymbolLib;
    private File fileSymbolLib = null;
    private SymbolList jList1 = null;

    public Trainer (StrokeList s1){
        this.generateDefaultSetSVM();



    }

    private void generateDefaultSetSVM() {
        try {
            this.objsymbolLib = SymbolLib.GenerateDefaultSetSVM(SymbolLib.LibraryTypes.Binary);
            jList1 = objsymbolLib.getSymbols();
            //jList1.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void generateDefaultSetElastic() {
        JFileChooser jfc = new JFileChooser();
        jfc.setSelectedFile(ConstantData.ElasticFileDefault);
        if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileSymbolLib = jfc.getSelectedFile();
            try {
                fileSymbolLib.createNewFile();
                SymbolLib.GenerateDefaultSetElastic(fileSymbolLib.getAbsolutePath(), SymbolLib.LibraryTypes.Binary);
                objSymbolLib = SymbolLib.Load(fileSymbolLib.getAbsolutePath(), SymbolLib.LibraryTypes.Binary);
                jList1.setListData(objSymbolLib.getSymbols().toArray());
                jList1.setSelectedIndex(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openSymbolLib() {
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        jfc.setSelectedFile(ConstantData.ElasticFile);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileSymbolLib = jfc.getSelectedFile();
            try {
                objSymbolLib = SymbolLib.Load(fileSymbolLib.getAbsolutePath(),
                        SymbolLib.LibraryTypes.Binary);
                jList1.setListData(objSymbolLib.getSymbols().toArray());
                jList1.setSelectedIndex(0);
            } catch (Exception ex) {
                objSymbolLib = null;
                JOptionPane.showMessageDialog(this, "Error in loading library.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSymbolLib() {
        if (fileSymbolLib != null && objSymbolLib != null) {
            try {
                objSymbolLib.setTitle(this.jTextField1.getText().trim());
                objSymbolLib.Save(fileSymbolLib.getAbsolutePath(), SymbolLib.LibraryTypes.Binary);
                JOptionPane.showMessageDialog(this, "Library is saved!.", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error in saving library.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
