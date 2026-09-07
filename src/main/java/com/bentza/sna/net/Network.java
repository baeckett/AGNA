package com.bentza.sna.net;

import com.bentza.sna.AgnaLog;
import com.bentza.sna.gui.AgnaTableModel;
import java.util.Vector;

        public class Network
    {
    private Vector all_nodes;

    private boolean symmetry;

    public String net_name; // numele retelei

    public Network()
        {
        symmetry = false;
        final int size = 10;
        // de aici incolo identic cu celalalt constructor:
        Actor tmp_node = null;
        String tmp_str = new String("");
        net_name = "New Network";
        all_nodes = new Vector(size);
        for (int i = 0; i < size; i++)
            {
            tmp_node = new Actor(String.valueOf(i + 1), size);
            all_nodes.addElement(tmp_node);
            }

        }

    public Network(float[][] tmp_mat) // construieste o retea in functie de o
                                        // matrice data
        {
        symmetry = false;
        Actor tmp_node = null;
        net_name = "New Network";
        all_nodes = new Vector(tmp_mat.length);
        for (int i = 0; i < tmp_mat.length; i++)
            {
            tmp_node = new Actor("Node " + String.valueOf(i + 1),
                    tmp_mat.length);
            for (int j = 0; j < tmp_mat.length; j++)
                {
                // tmp_node.emissions.setElementAt(String.valueOf(tmp_mat[i][j]),j);
                tmp_node.setEmissionsValue(tmp_mat[i][j], j);
                }
            all_nodes.addElement(tmp_node);
            }
        }

    public Network(int size)
        {
        symmetry = false;
        Actor tmp_node = null;
        net_name = "New Network";
        all_nodes = new Vector(size);
        for (int i = 0; i < size; i++)
            {
            tmp_node = new Actor(String.valueOf(i + 1), size);
            /*
             * Useless cycle: for (int j=0; j<size; j++) {
             * //tmp_node.emissions.setElementAt("0.0",j);
             * tmp_node.setEmissionsValue(0f, j); }
             * //tmp_node.moveActor(10+(int)((400-20)*Math.random()),10+(int)((400-20)*Math.random()),
             * 400, false);
             */
            all_nodes.addElement(tmp_node);
            }

        }

    // creates a network of a specific type:
    public Network(int size, int network_type)
    // network_type = 1 (symmetric star network)
        {
        if (network_type == 1)
            {
            symmetry = false;
            Actor tmp_node = null;
            net_name = "Star Network";
            all_nodes = new Vector(size);

            // creating central node:
            tmp_node = new Actor(String.valueOf(1), size);
            for (int j = 0; j < size; j++)
                {
                // tmp_node.emissions.setElementAt("1.0",j);
                tmp_node.setEmissionsValue(1f, j);
                }
            all_nodes.addElement(tmp_node);

            // creating all other nodes:
            for (int i = 1; i < size; i++)
                {
                tmp_node = null;
                tmp_node = new Actor(String.valueOf(i + 1), size);
                // tmp_node.emissions.setElementAt("1.0",0);
                tmp_node.setEmissionsValue(1f, 0);
                for (int j = 1; j < size; j++)
                    {
                    // tmp_node.emissions.setElementAt("0.0",j);
                    tmp_node.setEmissionsValue(0f, j);
                    }
                all_nodes.addElement(tmp_node);
                }
            }
        }

    public int getSize() // returneaza numarul de noduri al retelei
        {
        return all_nodes.size();
        }

    public String getName()
        {
        try
            {
            return net_name;
            } catch (Exception e)
            {
            return "";
            }
        }

    public void setName(String tmp_name)
        {
        net_name = tmp_name;
        }

    public float getMin() // returneaza cel mai mic element din matrice
        {
        int n = all_nodes.size();
        float min = Float.POSITIVE_INFINITY;
        // 2.1.3: iterate the nodes directly instead of materializing the full
        // matrix (avoided allocation per call); an all-zero matrix yields 0f
        // instead of +Infinity
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                float value = getValue(i, j);
                if (value != 0f && min > value)
                    {
                    min = value;
                    }
                }
            }
        return min == Float.POSITIVE_INFINITY ? 0f : min;
        }

    public float getMax() // returneaza cel mai mic element din matrice
        {
        int n = all_nodes.size();
        float max = Float.NEGATIVE_INFINITY;
        // 2.1.3: same as getMin (no matrix allocation; all-zero -> 0f)
        for (int i = 0; i < n; i++)
            {
            for (int j = 0; j < n; j++)
                {
                float value = getValue(i, j);
                if (value != 0f && max < value)
                    {
                    max = value;
                    }
                }
            }
        return max == Float.NEGATIVE_INFINITY ? 0f : max;
        }

    public boolean getSymmetry()
        {
        return symmetry;
        }

    public void setSymmetry(boolean tmp_symmetry)
        {
        symmetry = tmp_symmetry;
        }

    // checks the symmetry of the network;
    // changes symmetry value;
    public boolean isSymmetric()
        {
        int i, j, n;
        boolean finval = true;
        n = all_nodes.size();
        // 2.1.3: no full-matrix allocation; compare cells directly
        for (i = 0; i < n; i++)
            {
            for (j = i; j < n; j++)
                {
                if (getValue(i, j) != getValue(j, i))
                    {
                    finval = false;
                    return finval;
                    }
                }
            }
        setSymmetry(finval);
        return finval;
        }

    public float getValue(int i, int j) // returneaza valoarea elementului (i,j)
                                        // din matricea retelei
        {
        int nn = this.getSize();
        if (i < 0 || j < 0 || i >= nn || j >= nn)
            {
            return 0f;
            }
        // 2.1.3: removed a pointless "new Actor()" allocation that crashed
        // headless (and wasted a node per cell read); only the real node is used
        Actor tmp_node = getActor(i);
        if (tmp_node == null)
            {
            return 0f;
            }
        return tmp_node.getEmissionsValue(j);
        }

    public void setValue(float tmp_value, int i, int j) // schimba valoarea
                                                        // elementului (i,j) din
                                                        // matricea retelei
{
        int nn = this.getSize();
        if (i >= nn || j >= nn)
            return;
        // 2.1.3: removed a pointless "new Actor()" allocation (same as getValue)
        Actor tmp_node = getActor(i);
        if (tmp_node == null)
            {
            return;
            }
        tmp_node.setEmissionsValue(tmp_value, j);
        }

    /*
     * public void setStringValue(String tmp_value, int i, int j) // schimba
     * valoarea elementului (i,j) din matricea retelei { Actor tmp_node=new
     * Actor(); tmp_node=(Actor)all_nodes.elementAt(i);
     * tmp_node.setStringEmissionsValue(tmp_value, j); } public void
     * setStringValue(Object tmp_value, int i, int j) // schimba valoarea
     * elementului (i,j) din matricea retelei { Actor tmp_node=new Actor();
     * tmp_node=(Actor)all_nodes.elementAt(i);
     * tmp_node.setStringEmissionsValue(tmp_value, j); }
     */

    public void setObjectValue(Object tmp_value, int i, int j) // schimba
                                                                // valoarea
                                                                // elementului
                                                                // (i,j) din
                                                                // matricea
                                                                // retelei
        {
        // 2.1.3: removed a pointless "new Actor()" allocation (same as getValue)
        Actor tmp_node = getActor(i);
        if (tmp_node == null)
            {
            return;
            }
        try
            {
            tmp_node.setObjectEmissionsValue((Float) tmp_value, j);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        }

    // returns emissions array of node i
    public float[] getEmissions(int i)
        {
        float[] tmp_emis = new float[this.getSize()];
        Actor tmp_node = (Actor) all_nodes.elementAt(i);
        tmp_emis = tmp_node.getEmissionsArray(this.getSize());
        return tmp_emis;
        }

    public boolean hasNoEmission(int i)
        {
        Actor tmp_node = (Actor) all_nodes.elementAt(i);
        return tmp_node.hasNoEmission(this.getSize());
        }

    public boolean hasNoReception(int j)
        {
        boolean has_no_reception = true;
        for (int i = 0; i < this.getSize(); i++)
            {
            if (this.getValue(i, j) != 0f)
                {
                has_no_reception = false;
                break;
                }
            }

        return has_no_reception;
        }

    // deletes all connections from node i to node i
    public void deleteReflections()
        {
        int nn = this.getSize();
        for (int i = 0; i < nn; i++)
            {
            if (getValue(i, i) != 0f)
                setValue(0f, i, i);
            }
        }

    public void setStringMatrix(String[][] tmp_matrix)
        {
        for (int i = 0; i < getSize(); i++)
            {
            for (int j = 0; j < getSize(); j++)
                {
                setObjectValue(new Float(tmp_matrix[i][j]), i, j);
                }
            }

        }

    public void setMatrix(float[][] tmp_matrix)
        {
        for (int i = 0; i < getSize(); i++)
            {
            for (int j = 0; j < getSize(); j++)
                {
                setValue(tmp_matrix[i][j], i, j);
                }
            }

        }

    public float[][] getMatrix() // returneaza matricea retelei
        {
        int i, j, n;
        n = all_nodes.size();
        float[][] mat = new float[n][n];
        Actor cursor_actor;
        for (i = 0; i < n; i++)
            {
            cursor_actor = (Actor) all_nodes.elementAt(i);
            for (j = 0; j < n; j++)
                {
                mat[i][j] = cursor_actor.getEmissionsValue(j);
                // mat[i][j]=this.getValue(i,j);
                }
            }
        return mat;
        }

    /**
     * invoked before destroying network
     */
    public void cleaning()
        {
        Actor tmp_node = null;
        for (int i = 0; i < this.getSize(); i++)
            {
            tmp_node = getActor(i);
            tmp_node.cleaning();
            tmp_node = null;
            }
        all_nodes = null;
        net_name = null;
        }

    /**
     * returns the boolean version of the sociomatrix
     */
    public boolean[][] getBooleanMatrix()
        {
        int i, j, n;
        n = this.getSize();
        boolean[][] mat = new boolean[n][n];
        Actor tmp_node = null;

        for (i = 0; i < n; i++)
            {
            for (j = 0; j < n; j++)
                {
                try
                    {
                    tmp_node = (Actor) all_nodes.elementAt(i);
                    if (tmp_node.getEmissionsValue(j) == 0f)
                        mat[i][j] = false;
                    else
                        mat[i][j] = true;
                    } catch (ArrayIndexOutOfBoundsException e1)
                    {
                    }

                }
            }
        return mat;
        }

    // forces matrix to integer values
    public int[][] getIntegerMatrix()
        {
        int i, j, n;
        n = this.getSize();
        int[][] mat = new int[n][n];
        for (i = 0; i < n; i++)
            {
            for (j = 0; j < n; j++)
                {
                mat[i][j] = (int) this.getValue(i, j);
                }
            }
        return mat;
        }

    public int getEdgesNumber() // returns number of edges
        {
        int i, j, n, n_edges;
        n = this.getSize();
        n_edges = 0;
        for (i = 0; i < n; i++)
            {
            for (j = 0; j < n; j++)
                {
                if (this.getValue(i, j) != 0f)
                    n_edges++;
                }
            }
        return n_edges;
        }

    public void setNodeName(String tmp_name, int i)
        {
        Actor tmp_node = new Actor();
        tmp_node = (Actor) all_nodes.elementAt(i);
        tmp_node.setName(tmp_name);
        }

    public boolean setNodeNames(String[] node_names)
        {
        int nn = node_names.length;
        if (nn != all_nodes.size())
            return false; // errors encountered

        Actor tmp_node = null;
        for (int i = 0; i < nn; i++)
            {
            tmp_node = (Actor) all_nodes.elementAt(i);
            tmp_node.setName(node_names[i]);
            }

        return true; // successful return
        }

    /**
     * returns node names as an array of strings
     */
    public String[] getNodeNames()
        {
        Actor tmp_node = null;
        String[] nnames = new String[getSize()];
        for (int i = 0; i < getSize(); i++)
            {
            tmp_node = (Actor) all_nodes.elementAt(i);
            nnames[i] = tmp_node.getName();
            }
        tmp_node = null;
        return nnames;
        }

    /**
     * returns node names as an array of strings; names are enclosed within
     * quotes;
     */
    public String[] getNodeNamesInQuotes()
        {
        Actor tmp_node = null;
        String[] nnames = new String[getSize()];
        for (int i = 0; i < getSize(); i++)
            {
            tmp_node = (Actor) all_nodes.elementAt(i);
            nnames[i] = tmp_node.getNameInQuotes();
            }
        return nnames;
        }

    // returns the first node whose name is tmp_node_name
    public int getActor(String tmp_node_name)
        {
        int i = 0;
        int nn = this.getSize();
        while (i < nn)
            {
            if (getActor(i).getName().equals(tmp_node_name))
                {
                return i;
                }
            i++;
            }
        return -1;
        }

    // searches for actors that contain name_piece in their names
    // returns an IntList
    // NOT cap sensitive
    public IntList searchActors(String name_piece)
        {
        int i = 0;
        int index = 0;
        int nn = all_nodes.size();
        IntList results = new IntList();
        name_piece = name_piece.toLowerCase();
        while (i < nn)
            {
            try
                {
                index = ((Actor) all_nodes.elementAt(i)).getName()
                        .toLowerCase().indexOf(name_piece);
                if (index >= 0)
                    {
                    results.appendValue(index);
                    }
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            i++;
            }
        return results;
        }

    // searches for actors that contain name_piece in their names
    // returns an integer
    // starts with start_node
    // NOT cap sensitive
    public int searchNextActorNotCapsSensitive(String name_piece,
            int start_node, boolean match_name)
        {
        int i = start_node;
        int index = -1;
        int nn = all_nodes.size();

        name_piece = name_piece.toLowerCase();

        if (match_name)
            {
            while (i < nn)
                {
                try
                    {
                    if (name_piece.equals(((Actor) all_nodes.elementAt(i))
                            .getName().toLowerCase()))
                        {
                        break;
                        }
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                i++;
                } // end while
            } // end if
        else
            {
            while (i < nn && index < 0)
                {
                try
                    {
                    index = ((Actor) all_nodes.elementAt(i)).getName()
                            .toLowerCase().indexOf(name_piece);
                    if (index >= 0)
                        {
                        break;
                        }
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                i++;
                }// end while
            } // end else

        if (i < nn)
            return i;
        else
            return -1;
        }

    // searches for actors that contain name_piece in their names
    // returns an integer
    // starts with start_node
    // cap sensitive
    public int searchNextActorCapsSensitive(String name_piece, int start_node,
            boolean match_name)
        {
        int i = start_node;
        int index = -1;
        int nn = all_nodes.size();

        if (match_name)
            {
            while (i < nn)
                {
                try
                    {
                    if (name_piece.equals(((Actor) all_nodes.elementAt(i))
                            .getName()))
                        {
                        break;
                        }
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                i++;
                } // end while
            } // end if
        else
            {
            while (i < nn && index < 0)
                {
                try
                    {
                    index = ((Actor) all_nodes.elementAt(i)).getName().indexOf(
                            name_piece);
                    if (index >= 0)
                        {
                        break;
                        }
                    } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
                i++;
                }// end while
            } // end else

        if (i < nn)
            return i;
        else
            return -1;
        }

    public Vector getNodes()
        {
        return all_nodes;
        }

    public String getActorName(int tmp_i)
        {
        try
            {
            Actor tmp_actor = (Actor) all_nodes.elementAt(tmp_i);
            return tmp_actor.getName();
            } catch (Exception e)
            {
            return "";
            }
        }

    /**
     * Returns the index of a given actor; returns -1 if actor does not belong
     * to this Network;
     */
    public int getActorIndex(Actor tmp_actor)
        {
        if (tmp_actor == null)
            return -1;

        int nn = getSize();
        Actor cursor_actor;
        for (int i = 0; i < nn; i++)
            {
            try
                {
                cursor_actor = (Actor) all_nodes.elementAt(i);
                if (cursor_actor == tmp_actor)
                    {
                    return i;
                    }
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            }
        return -1;
        }

    public Actor getActor(int tmp_i)
        {
        try
            {
            return (Actor) all_nodes.elementAt(tmp_i);
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        return null;
        }

    public String getNodeName(int tmp_i)
        {
        try
            {
            return ((Actor) all_nodes.elementAt(tmp_i)).getName();
            } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
        return null;
        }

    public void cloneActor(int clone, int area_width)
        {
        if (clone < 0 || clone >= getSize())
            return;
        addActor(-1, -1, area_width, true); // no position specified
        int t_size = getSize();
        Actor tmp_node = new Actor();
        Actor cloned_node = new Actor(); // initial node
        cloned_node = getActor(clone);
        tmp_node = getActor(t_size - 1); // new node
        for (int i = 0; i < t_size; i++)
            {
            // tmp_node.emissions.setElementAt(cloned_node.emissions.elementAt(i),
            // i);
            tmp_node.setEmissionsValue(cloned_node.getEmissionsValue(i), i);
            for (int j = 0; j < t_size; j++)
                {
                // getActor(j).emissions.setElementAt(getActor(j).emissions.elementAt(clone),
                // t_size-1);
                getActor(j).setEmissionsValue(
                        getActor(j).getEmissionsValue(clone), t_size - 1);
                }
            }
        // tmp_node.emissions.setElementAt("0.0", clone);
        tmp_node.setEmissionsValue(0f, clone);
        // cloned_node.emissions.setElementAt("0.0", t_size-1);
        cloned_node.setEmissionsValue(0f, t_size - 1);
        tmp_node.name = "Clone of " + cloned_node.name;
        tmp_node.setFace(cloned_node.getFaceSource());
        tmp_node.setSize(cloned_node.getSize());
        tmp_node.moveActor(cloned_node.getX(area_width)
                + (int) (40f * (float) Math.random()), cloned_node
                .getY(area_width)
                - (int) (40f * (float) Math.random()), area_width - 40, false);
        }

    public void setSize(int tmp_size)
        {
        all_nodes.setSize(tmp_size);
        Actor tmp_node = null;
        for (int i = 0; i < tmp_size; i++)
            {
            tmp_node = this.getActor(i);
            tmp_node.setEmissionsNumber(tmp_size);
            }
        }

    public void addActor(String tmp_node_name)
        {
        int i = 0;
        int new_i = 0, t_size = 0;
        t_size = getSize();
        new_i = 1;
        // adding new node to all_nodes vector:
        Actor new_node = new Actor(tmp_node_name, t_size);
        all_nodes.addElement(new_node);
        // restructuring emission vectors of other nodes:
        t_size = getSize();
        for (i = 0; i < t_size; i++)
            {
            if (getActor(i) != null)
                {
                getActor(i).addEmissionsElement();
                }
            }
        }

    // generates a new name, different from all previous names
    private String getNewNodeName(int start)
        {
        int new_i = start;
        String tmp_name = "New " + String.valueOf(new_i);
        // verifica daca numele noului nod exista deja:
        for (int i = 0; i < this.getSize(); i++)
            {
            if (tmp_name.equals(getActor(i).name))
                {
                new_i++;
                tmp_name = getNewNodeName(new_i);
                }
            }
        return tmp_name;
        }

    /**
     * Adds one actor to network; position specified;
     */
    public void addActor(int tmp_x, int tmp_y, int tmp_x_max, boolean is_area)
        {
        int nn = all_nodes.size();
        // adding new node to all_nodes vector:
        Actor new_node = new Actor(getNewNodeName(1), nn);
        if (is_area)
            {
            new_node.createCoordinates();
            }

        if (tmp_x != -1 && tmp_y != -1 && tmp_x_max != -1)
            {
            new_node.setX(tmp_x, tmp_x_max);
            new_node.setY(tmp_y, tmp_x_max);
            }
        // new_node.setFace(getActor(0).getFaceSource());
        // new_node.setSize(getActor(0).getSize());
        all_nodes.addElement(new_node);

        nn = all_nodes.size();
        // restructuring emission vectors of other nodes:
        for (int i = 0; i < nn; i++)
            {
            try
                {
                new_node = (Actor) all_nodes.elementAt(i);
                new_node.addEmissionsElement();
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            /*
             * if (getActor(i) != null) { getActor(i).addEmissionsElement(); }
             */
            }
        }

    /**
     * Adds a number of actors to network; position not specified; not optimized -
     * to be revised!
     */
    public void addActors(int n_actors, boolean is_area)
        {
        if (n_actors <= 0)
            return;

        for (int k = 0; k < n_actors; k++)
            addActor(-1, -1, -1, is_area);
        }

    public void deleteActor(int tmp_i)
        {
        if (tmp_i >= getSize() || tmp_i < 0 || getSize() < 3)
            return;
        Actor tmp_node = getActor(tmp_i);
        tmp_node.cleaning();
        tmp_node = null;
        all_nodes.remove(tmp_i);
        int nn = all_nodes.size();
        for (int i = 0; i < nn; i++)
            {
            try
                {
                tmp_node = (Actor) all_nodes.elementAt(i);
                tmp_node.deleteEmissionsElement(tmp_i);
                } catch (Exception e) {
      AgnaLog.warn("suppressed exception", e);
      }
            }
        }

    public void isolateActor(int tmp_i)
        {
        int nn = this.getSize();
        if (tmp_i >= nn || tmp_i < 0)
            return;
        for (int i = 0; i < nn; i++)
            {
            setValue(0f, i, tmp_i);
            setValue(0f, tmp_i, i);
            }
        }

    public boolean isOutsider(int tmp_i)
        {
        boolean finval = true;
        int i = 0;
        int nn = this.getSize();
        for (i = 0; i < nn; i++)
            {
            // returns false if at least one of the node's
            // emissions or receptions is non zero.
            if (this.getValue(tmp_i, i) != 0 || this.getValue(i, tmp_i) != 0)
                {
                finval = false;
                return finval;
                }
            }
        return finval;
        }

    public void removeOutsiders(AgnaTableModel tmp_model)
        {
        int i = 0;
        int max = 0;
        do
            {
            if (isOutsider(i) && this.getSize() > 2)
                {
                deleteActor(i);
                tmp_model.delRowCol(i);
                } else
                i++;
            max = this.getSize();
            } while (i < max && max > 2);
        }
    }