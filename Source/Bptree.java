package bptree;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Bptree {
    static class Node {
        int m;
        List<Pair> p;
        Node r;
        int parentnum = 0;
        boolean isconnected = false;

        static class Pair {

            int key;
            int value;
            Node child;

            public Pair(int key, int value, Node child) {
                this.key = key;
                this.value = value;
                this.child = child;
            }
        }
        Node() {
            this.m = 0;
            this.p = new ArrayList();
            this.r = null;
            this.parentnum = 0;
            this.isconnected = false;
        }
    }

    private static Node root = new Node();
    private static int degree;
    private static int mDegree;
    private static int nodeindex = 0;
    private static Node parentNode;
    private static int parentIndex;
    private static List<Integer> leafIndex = new ArrayList<>();
    private static Boolean flag = true;

    public static void main(String[] args) {

        switch (args[0]) {

            case "-c":
                degree = Integer.parseInt(args[2]);
                mDegree = degree - 1;
                create(args[1], degree);
                break;

            case "-i":
                insertion(args[1], args[2]);
                break;

            case "-d":
                deletion(args[1], args[2]);
                break;

            case "-s":
                single_key_search(args[1],Integer.parseInt(args[2]));
                break;

            case "-r":
                ranged_search(args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]));
                break;
        }
    }

    public static void create(String index_file, int degree) {
        try {
            BufferedWriter indexfile = new BufferedWriter(new FileWriter(index_file, false));
            indexfile.write(degree + "\n");
            indexfile.flush();
            indexfile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void insertion(String index_file, String input_file) {
        File inputFileReader = new File(input_file);
        File indexFileReader = new File(index_file);
        loadtree(indexFileReader);

        try {
            BufferedReader inputReader = new BufferedReader(new FileReader(inputFileReader));

            String line;

            while ((line = inputReader.readLine()) != null) {
                String[] datalist = line.split(",");

                int key = Integer.parseInt(datalist[0]);
                int value = Integer.parseInt((datalist[1]));

                insertTree(key, value);
            }

            inputReader.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        save(indexFileReader);


    }

    public static void insertTree(int key, int value) {

        Node node = root;
        int addIndex;

        while (true) {
            addIndex = node.m;
            for (int i = 0; i < node.m; i++) {
                if (node.p.get(i).key > key) {
                    addIndex = i;
                    break;
                }
            }
            if (node.m == 0 || node.p.get(0).child == null) break;

            if (addIndex < node.m && node.p.get(addIndex).child != null) node = node.p.get(addIndex).child;
            else if (addIndex == node.m && node.r != null) node = node.r;
        }

        node.m++;
        node.p.add(addIndex, new Node.Pair(key, value, null));

        if (node.m >= mDegree + 1) leafSplit(node);
    }

    public static void leafSplit(Node node) {
        int leftnum = (mDegree + 1) / 2;
        Node rightnode = new Node();
        int parentkey = node.p.get(leftnum).key;
        int parentvalue = node.p.get(leftnum).value;
        rightnode.r = node.r;

        if ((mDegree + 1) % 2 == 0) rightnode.m = leftnum;
        else rightnode.m = leftnum + 1;

        for (int i = leftnum; i < mDegree + 1; i++) {
            rightnode.p.add(new Node.Pair(node.p.get(i).key, node.p.get(i).value, node.p.get(i).child));
        }

        if (node == root) {
            Node leftnode = new Node();

            for (int i = 0; i < leftnum; i++) {
                leftnode.p.add(new Node.Pair(root.p.get(i).key, root.p.get(i).value, root.p.get(i).child));
            }

            leftnode.m = leftnum;
            leftnode.r = rightnode;

            root.m = 1;
            root.r = rightnode;
            root.p.clear();
            root.p.add(new Node.Pair(parentkey, parentvalue, leftnode));
        }
        else {
            node.r = rightnode;
            node.p.subList(leftnum, mDegree + 1).clear();
            node.m = leftnum;

            parentNode = null;
            findParentIndex(root, node);
            parentNode.m++;
            parentNode.p.add(parentIndex, new Node.Pair(parentkey, parentvalue, node));

            if (parentIndex == parentNode.m - 1) parentNode.r = rightnode;
            else parentNode.p.get(parentIndex + 1).child = rightnode;

            if (parentNode.m >= mDegree + 1) nonleafSplit(parentNode);
        }
    }

    public static void nonleafSplit(Node node) {
        int leftnum = node.m / 2;
        Node rightnode = new Node();
        int parentkey = node.p.get(leftnum).key;
        int parentvalue = node.p.get(leftnum).value;
        rightnode.r = node.r;

        if (node.m % 2 == 0) rightnode.m = leftnum - 1;
        else rightnode.m = leftnum;

        for (int i = leftnum + 1; i < node.m; i++) {
            rightnode.p.add(new Node.Pair(node.p.get(i).key, node.p.get(i).value, node.p.get(i).child));
        }
        if (node == root) {
            Node leftnode = new Node();

            for (int i = 0; i < leftnum; i++) {
                leftnode.p.add(new Node.Pair(root.p.get(i).key, root.p.get(i).value, root.p.get(i).child));
            }

            leftnode.m = leftnum;
            leftnode.r = node.p.get(leftnum).child;

            root.m = 1;
            root.r = rightnode;
            root.p.clear();
            root.p.add(new Node.Pair(parentkey, parentvalue, leftnode));

        }
        else {
            node.r = node.p.get(leftnum).child;
            node.p.subList(leftnum, node.m).clear();
            node.m = leftnum;

            parentNode = null;
            findParentIndex(root, node);
            parentNode.m++;
            parentNode.p.add(parentIndex, new Node.Pair(parentkey, parentvalue, node));

            if (parentIndex == parentNode.m - 1) parentNode.r = rightnode;
            else parentNode.p.get(parentIndex + 1).child = rightnode;

            if (parentNode.m >= mDegree + 1) nonleafSplit(parentNode);
        }
    }

    public static void single_key_search(String index_file, int key) {
        File indexfiler = new File(index_file);
        loadtree(indexfiler);

        Node node = root;
        while (true) {
            int index = node.m;
            for (int i = 0; i < node.m; i++) {

                if (node.p.get(0).child != null && node.p.get(0).child.p.get(0).child == null && node.p.get(i).key >= key) {
                    if (node.p.get(0).child != null) System.out.print(node.p.get(i).key);
                    index = i;
                    break;
                }
                else if (node.p.get(i).key >= key) {
                    if (node.p.get(0).child != null) System.out.print(node.p.get(i).key + ",");
                    index = i;
                    break;
                }
                else if (node.p.get(0).child != null) System.out.print(node.p.get(i).key + ",");
            }

            if (node.p.get(0).child == null) {
                if (index != node.m && node.p.get(index).key == key) {
                    System.out.println();
                    System.out.print(node.p.get(index).value);
                    break;
                }
                else {
                    System.out.println();
                    System.out.print("NOT FOUND");
                    break;
                }
            }
            else if (index == node.m) {
                node = node.r;
            }
            else {
                if (node.p.get(index).key == key && index < node.m - 1) {
                    node = node.p.get(index + 1).child;
                }
                else if (node.p.get(index).key == key && index == node.m - 1) {
                    node = node.r;
                }
                else node = node.p.get(index).child;
            }
        }
    }

    public static void ranged_search(String index_file, int startNum, int endNum) {
        File indexfiler = new File(index_file);
        loadtree(indexfiler);

        if (startNum > endNum) System.out.println("Incorrect range");

        Node node = root;

        while (true) {
            int index = node.m;
            for (int i = 0; i < node.m; i++) {
                if (node.p.get(i).key >= startNum) {
                    index = i;
                    break;
                }
            }

            if (node.p.get(0).child == null) {
                boolean flag = false;
                while (true) {
                    for (int i = index; i < node.m; i++) {
                        if (node.p.get(i).key > endNum) {
                            flag = true;
                            break;
                        }
                        System.out.println(node.p.get(i).key + "," + node.p.get(i).value);
                    }

                    if (node.r != null) node = node.r;
                    else flag = true;
                    if (flag) break;
                    index = 0;
                }
                break;
            }
            if (index == node.m) {
                node = node.r;
            }
            else node = node.p.get(index).child;
        }
    }

    public static void deletion(String index_file, String delete_file) {
        File deleteFiler = new File(delete_file);
        File indexFiler = new File(index_file);
        loadtree(indexFiler);

        try {
            BufferedReader inputReader = new BufferedReader(new FileReader(deleteFiler));

            String line;

            while ((line = inputReader.readLine()) != null) {

                int key = Integer.parseInt(line);

                deleteTree(key);
            }

            inputReader.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        save(indexFiler);
    }

    public static void deleteTree(int key) {
        Node node = root;
        int index;

        while (true) {
            index = node.m;
            for (int i = 0; i < node.m; i++) {
                if (node.p.get(i).key > key) {
                    index = i;
                    break;
                }
            }
            if (node.m == 0 || node.p.get(0).child == null) break;

            if (index == node.m) node = node.r;
            else node = node.p.get(index).child;
        }

        index = -1;

        for (int i = 0; i < node.m; i++) {
            if (key == node.p.get(i).key) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.out.println("The key was not found.");
            return;
        }

        Node leftleaf = findLeftLeaf(node.p.get(0).key);

        int changeKey = node.p.get(0).key;

        node.p.remove(index);
        node.m--;

        if (node.m >= mDegree / 2) {

            if (index == 0) swapKey(key, node.p.get(0).key, node.p.get(0).value);
            return;
        }

        Node leftSibiling = findLeftSibiling(node);
        Node rightSibiling = findRightSibling(node);

        if (rightSibiling != null && rightSibiling.m - 1 >= mDegree / 2) {
            takeRight(node, rightSibiling, changeKey, index);
        }
        else if (leftSibiling != null && leftSibiling.m - 1 >= mDegree / 2) {
            takeLeft(node, leftSibiling, changeKey);
        }
        else {
            mergeLeaf(node, rightSibiling, leftSibiling, leftleaf, key, index);
        }
    }

    public static void takeRight(Node node, Node rightSibiling, int changeKey, int index) {
        node.p.add(new Node.Pair(rightSibiling.p.get(0).key, rightSibiling.p.get(0).value, null));
        node.m++;

        rightSibiling.p.remove(0);
        rightSibiling.m--;

        swapKey(node.p.get(node.m - 1).key, rightSibiling.p.get(0).key, rightSibiling.p.get(0).value);
        if (index == 0) swapKey(changeKey, node.p.get(0).key, node.p.get(0).value);

    }

    public static void takeLeft(Node node, Node leftSibiling, int changeKey) {
        node.p.add(0, new Node.Pair(leftSibiling.p.get(leftSibiling.m - 1).key, leftSibiling.p.get(leftSibiling.m - 1).value, null));
        node.m++;

        leftSibiling.p.remove((leftSibiling.m - 1));
        leftSibiling.m--;

        swapKey(changeKey, node.p.get(0).key, node.p.get(0).value);
    }

    public static void mergeLeaf(Node node, Node rightSibiling, Node leftSibiling, Node leftleaf, int key, int index) {
        parentNode = null;
        findParentIndex(root, node);

        if (parentNode != null) {
            if (rightSibiling != null) {

                for (int i = node.m - 1; i >= 0; i--) {
                    rightSibiling.p.add(0, new Node.Pair(node.p.get(i).key, node.p.get(i).value, null));
                    rightSibiling.m++;
                }
                if (index == 0) swapKey(key, rightSibiling.p.get(0).key, rightSibiling.p.get(0).value);

                if (leftleaf != null) leftleaf.r = rightSibiling;

                parentNode.p.remove(parentIndex);
                parentNode.m--;

                if (parentNode == root) {
                    if (parentNode.m > 0) {
                        return;
                    } else {
                        root = rightSibiling;

                    }
                } else {
                    if (parentNode.m > mDegree / 2)
                        return;
                    else mergeNonLeaf(parentNode);
                }
            } else if (leftSibiling != null) {
                for (int i = 0; i < node.m; i++) {
                    leftSibiling.p.add(new Node.Pair(node.p.get(i).key, node.p.get(i).value, null));
                    leftSibiling.m++;
                }
                leftSibiling.r = node.r;

                if (parentIndex == parentNode.m)
                    parentNode.r = leftSibiling;
                else
                    parentNode.p.get(parentIndex).child = leftSibiling;

                parentNode.p.remove(parentIndex - 1);
                parentNode.m--;

                if (parentNode == root) {
                    if (parentNode.m > 0)
                        return;
                    else root = leftSibiling;
                } else {
                    if (parentNode.m >= mDegree / 2) return;
                    else mergeNonLeaf(parentNode);
                }
            }
        }
    }

    public static void mergeNonLeaf(Node node) {
        parentNode = null;
        findParentIndex(root, node);

        if (parentNode != null) {
            Node leftSibiling = findLeftSibiling(node);
            Node rightSibiling = findRightSibling(node);

            if (rightSibiling != null) {
                int key = parentNode.p.get(parentIndex).key;
                int value = parentNode.p.get(parentIndex).value;

                rightSibiling.p.add(0, new Node.Pair(key, value, node.r));
                rightSibiling.m++;
                for (int i = node.m - 1; i >= 0; i--) {
                    rightSibiling.p.add(0, new Node.Pair(node.p.get(i).key, node.p.get(i).value, node.p.get(i).child));
                    rightSibiling.m++;
                }

                parentNode.p.remove(parentIndex);
                parentNode.m--;

                if (rightSibiling.m > mDegree) nonleafSplit(rightSibiling);

                if (parentNode == root) {
                    if (parentNode.m > 0)
                        return;
                    else root = rightSibiling;
                } else {
                    if (parentNode.m >= mDegree / 2) return;
                    else mergeNonLeaf(parentNode);
                }

            } else if (leftSibiling != null) {
                int key = parentNode.p.get(parentIndex - 1).key;
                int value = parentNode.p.get(parentIndex - 1).value;

                parentNode.p.remove((parentIndex - 1));
                parentNode.m--;

                leftSibiling.p.add(new Node.Pair(key, value, leftSibiling.r));
                leftSibiling.m++;

                for (int i = 0; i < node.m; i++) {
                    leftSibiling.p.add(new Node.Pair(node.p.get(i).key, node.p.get(i).value, node.p.get(i).child));
                    leftSibiling.m++;
                }

                leftSibiling.r = node.r;

                if (parentNode.m > 0 && parentIndex - 1 != parentNode.m)
                    parentNode.p.get(parentIndex - 1).child = leftSibiling;
                else parentNode.r = leftSibiling;

                if (leftSibiling.m > mDegree) nonleafSplit(leftSibiling);

                if (parentNode == root) {
                    if (parentNode.m > 0)
                        return;
                    else root = leftSibiling;
                } else {
                    if (parentNode.m >= mDegree / 2) return;
                    else mergeNonLeaf(parentNode);
                }
            }
        }
    }

    public static void swapKey(int beforeKey, int afterKey, int afterValue) {
        Node node = root;

        while (true) {

            int index = node.m;

            if (node.m == 0 || node.p.get(0).child == null)
                break;

            for (int i = 0; i < node.m; i++) {

                if (beforeKey <= node.p.get(i).key) {

                    if (beforeKey == node.p.get(i).key) {

                        node.p.get(i).key = afterKey;
                        node.p.get(i).value = afterValue;
                        return;
                    }
                    index = i;
                    break;
                }
            }
            if (index == node.m)
                node = node.r;
            else node = node.p.get(index).child;

        }
    }

    public static Node findLeftSibiling(Node node) {
        parentNode = null;
        findParentIndex(root, node);

        if (parentNode != null) {
            if (parentIndex == 0) {
                return null;
            } else {
                return parentNode.p.get(parentIndex - 1).child;
            }
        }
        return null;
    }

    public static Node findRightSibling(Node node) {
        parentNode = null;
        findParentIndex(root, node);

        if (parentNode != null) {
            if (parentNode.m == parentIndex) return null;
            else if (parentNode.m == parentIndex + 1) return parentNode.r;
            else return parentNode.p.get(parentIndex + 1).child;
        }
        return null;
    }

    public static Node findLeftLeaf(int find_key) {

        Node node = root;
        Node firstNode = root;
        while (firstNode.p.get(0).child != null) {
            firstNode = firstNode.p.get(0).child;

        }
        for (int i = 0; i < firstNode.m; i++) {
            if (firstNode.p.get(i).key == find_key) return null;
        }
        while (true) {
            int index = node.m;

            if (node.m == 0 || node.p.get(0).child == null)
                return node;

            for (int i = 0; i < node.m; i++) {

                if (find_key <= node.p.get(i).key) {

                    index = i;
                    break;
                }
            }
            if (index == node.m) {
                node = node.r;
            } else node = node.p.get(index).child;
        }
    }

    public static void findParentIndex(Node parent, Node child) {

        if (parent == null || parentNode != null) return;
        int key = 0;

        if (child.m != 0) {
            key = child.p.get(0).key;
        }

        for (int i = 0; i < parent.m; i++) {

            if (parent.p.get(i).child == child) {
                parentNode = parent;
                parentIndex = i;
            }
            else findParentIndex(parent.p.get(i).child, child);

            if (child.m!=0 && parent.p.get(i).key > key) return;
        }

        if ((parent.m == 0 || parent.p.get(0).child != null) && parent.r != null) {

            if (parent.r == child) {

                parentNode = parent;
                parentIndex = parent.m;

            }
            else findParentIndex(parent.r, child);
        }
    }

    public static void loadtree(File index_file) {
        try {
            FileReader fr = new FileReader(index_file);
            BufferedReader writer = new BufferedReader(fr);
            String line;
            degree = Integer.parseInt(writer.readLine());
            mDegree = degree - 1;

            List<Node> tree = new ArrayList<>();

            while ((line = writer.readLine()) != null) {
                String[] indexline = line.split(" ");

                int parentIndexnum = Integer.parseInt(indexline[1]);
                int nowIndex = Integer.parseInt(indexline[2]);
                Node node = new Node();
                node.parentnum = parentIndexnum;
                line = writer.readLine();
                indexline = line.split(" ");
                flag = true;

                for (String nodeline : indexline) {

                    if (nodeline.equals("@")) {
                        leafIndex.add(nowIndex);

                        for (int i = 0; i <= nowIndex - tree.size(); i++) tree.add(null);
                        tree.add(nowIndex, node);

                        flag = false;
                        if (node.parentnum == 0) {
                            node.isconnected = true;
                            break;
                        }
                        while (!node.isconnected) {
                            Node parentnode = tree.get(node.parentnum);
                            for (int i = 0; i < parentnode.m; i++) {
                                if (parentnode.p.get(i).child == null) {
                                    parentnode.p.get(i).child = node;
                                    node.isconnected = true;
                                    break;
                                }
                            }
                            if (!node.isconnected) {
                                parentnode.r = node;
                                node.isconnected = true;
                            }

                            node = parentnode;
                        }
                    } else {
                        String[] data = nodeline.split(",");
                        int key = Integer.parseInt(data[0]);
                        int value = Integer.parseInt(data[1]);
                        node.m++;
                        node.p.add(new Node.Pair(key, value, null));
                        flag = true;
                    }
                }
                if (flag) {
                    if (parentIndexnum == 0) {
                        root = node;
                        root.isconnected = true;
                    }
                    for (int i = 0; i <= nowIndex - tree.size(); i++) tree.add(null);
                    tree.add(nowIndex, node);
                }
            }

            for (int i = 0; i < leafIndex.size() - 1; i++) {
                Node node = tree.get(leafIndex.get(i));
                node.r = tree.get(leafIndex.get(i + 1));
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File index_File) {

        try {
            FileWriter treeWriter = new FileWriter(index_File, false);

            treeWriter.write(degree + "\n");
            treeWriter.close();
            treeWriter = new FileWriter(index_File, true);

            recursivesave(treeWriter, root, 0);

            treeWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recursivesave(FileWriter treeWriter, Node node, int parent) {

        nodeindex += 1;
        int nownodeindex = nodeindex;

        try {
            if (node.m == 0) return;
            treeWriter.write("$ " + parent + " " + nownodeindex + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < node.m; i++) {
            try {
                treeWriter.write(node.p.get(i).key + "," + node.p.get(i).value + " ");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (node.p.get(0).child == null) {
                treeWriter.write("@");
            }
            treeWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < node.m; i++) {
            if (node.p.get(i).child != null) {
                recursivesave(treeWriter, node.p.get(i).child, nownodeindex);
            }
        }

        if (node.p.get(0).child != null && node.r != null) {
            recursivesave(treeWriter, node.r, nownodeindex);
        }

        if (node.p.get(0).child == null) {
            leafIndex.add(nownodeindex);
        }
    }
}