package main;

import java.util.ArrayList;

public class Node {

    private Token token;
    private ArrayList<Node> linkedNodes;

    public Node (Token token) {
        this.token = token;
        linkedNodes = new ArrayList<>();
    }

    public Node (Token token, ArrayList<Node> linkedNodes) {
        this.token = token;
        this.linkedNodes = linkedNodes;
    }

    public void addLink (Node node) {

        for (Node linked : linkedNodes) {
            if (linked.token == node.token) {
                for (Node sons : node.linkedNodes)
                    linked.addLink(sons);
                return;
            }
        }

        linkedNodes.add(node);

    }

    public float getScore () {

        float score = token.getTokenScore(), attributeScore = 0, multiplyer = 0f;
        boolean modified = false;

        for (Node node : linkedNodes) {
            switch (node.token.getType()) {

                case Token.TYPE_FEATURE:
                    score += node.getScore();
                    break;

                case Token.TYPE_ATTRIBUTE:
                    attributeScore += node.getScore();
                    break;

                case Token.TYPE_MODIFIER:
                    multiplyer += node.getScore();
                    modified = true;
                    break;

                case Token.TYPE_IGNORE:
                    break;

            }
        }

        if (!modified)
            multiplyer = 1f;

        return (score + attributeScore + score * attributeScore / 10f) * multiplyer;

    }

    public int getFeatureCount () {

        int features = 0;

        for (Node node : linkedNodes) {

            if (node.token.getType() == Token.TYPE_FEATURE)
                features++;

        }

        if (features == 0)
            features = 1;

        return features;

    }

    @Override
    public String toString() {

        StringBuilder string = new StringBuilder();
        string.append(token.getString());
        string.append(" (");

        for (Node node : linkedNodes) {
            string.append(" ");
            string.append(node.toString());
            string.append(" ");

        }

        string.append(")");

        return string.toString();

    }
}
