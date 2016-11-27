import java.util.*;

class TreeNode{
  FileDescriptor info;
  TreeNode parent;
  ArrayList<TreeNode> children;

  TreeNode(){
    parent = null;
    children = new ArrayList<>();
    info = new FileDescriptor();
  }

  TreeNode(TreeNode parent, FileDescriptor info){
    this.parent = parent;
    this.info = info;
    children = new ArrayList<>();

    if(parent!=null){
    	parent.insertChild(this);
    }
  }

  void insertChild(TreeNode child){
    children.add(child);
  }

  void removeChild(TreeNode child){
    children.remove(child);
  }

  String getName(){
    return info.filename;
  }

  boolean isFolder(){
    return info.isDir;
  }

  String displayContent(){
  	return info.content;
  }
}

class Tree{
  TreeNode root;

  Tree(){
    root = new TreeNode();
  }

  static TreeNode search(String name, TreeNode curr){
  	if(curr.getName().equals(name) && curr.info.isDir){
  		return curr;
  	}

  	else{
  		for(int i = 0; i < curr.children.size(); i++){
  			return search(name, curr.children.get(i));
  		}
  	}

  	return null;
  }
}

class FileDescriptor{
  String filename;
  boolean isDir;
  String content;

  FileDescriptor(){
    filename = "root";
    isDir = true;
    content = null;
  }

  FileDescriptor(String filename, boolean isDir){
    this.filename = filename;
    this.isDir = isDir;

    if(isDir){
    	this.setContent("");
    }
  }

  void setContent(String new_content){
  	content = new_content;
  }

  void appendContent(String new_content){
  	content += new_content;
  }

  void displayFileInfo(){
    System.out.println("Name : " + filename);
    System.out.print("File type : ");
    if(isDir){
      System.out.print("Folder");
    }
    else{
      System.out.print("Text File");
    }
  }
}

class FileSystem{

  enum Command {
    CD, MKDIR, RMDIR, EXIT, LS, INVALID, EDIT, CP
  }

  Tree tree;
  TreeNode current;
  Command command;
  String rest[];

  FileSystem(){
    tree = new Tree();
    current = tree.root;
  }

  void printDirectory(TreeNode curr){
    if(curr.parent != null){
      printDirectory(curr.parent);
    }

    System.out.print(curr.getName());
      System.out.print("/");
  }

  void parseCommand(String command_line){
    String splittedString[] = command_line.split(" ");

    if(inEnum(splittedString[0])){
      command = Command.valueOf(splittedString[0].toUpperCase());
      if(splittedString.length == 1){
        rest = null;
      }
      else{
        rest = Arrays.copyOfRange(splittedString, 1, splittedString.length);
      }
    }

    else{
      command = Command.valueOf("INVALID");
      rest = Arrays.copyOfRange(splittedString, 0, splittedString.length);
    }

    //for(int i = 0; i < rest.length; i++){
    //  System.out.println(rest[i]);
    //}
  }

  static boolean inEnum(String test) {

    for (Command c : Command.values()) {
        if (c.name().equals(test.toUpperCase())) {
            return true;
        }
    }

    return false;
  }

  void execute(){
    boolean found = false;
    boolean valid;

    switch(command){
      case CD:
        if(rest != null){
            String temp_rest[] = rest[0].split("/");
            TreeNode temp_curr = current;

            int k = 0;
            if(temp_rest.length > 1){
              if(temp_rest[1].equals("root")){
                temp_curr = tree.root;
                k = 2;
                found = true;
              }
            }

            for(; k < temp_rest.length; k++){
              found = false;

              if(temp_rest[k].equals("..")){
                if(temp_curr != tree.root){
                  temp_curr = temp_curr.parent;
                }
                found = true;
              }

              else{
                if(temp_curr.children.isEmpty()){
                  System.out.println("cd: " + rest[0] + ": No such file or directory");
                  return;
                }


                for(int i = 0; i < temp_curr.children.size(); i++){
                  if(temp_curr.children.get(i).getName().equals(temp_rest[k])){
                    if(temp_curr.children.get(i).isFolder()){
                      temp_curr = temp_curr.children.get(i);
                      found = true;
                      break;
                    }
                    else{
                      System.out.println("cd: " + temp_rest[0] +".txt: Not a directory");
                      return;
                    }
                  }

                  if(found){
                    break;
                  }
                }
              }
    
            }

            if(found){
              current = temp_curr;
              break;
            }
            System.out.println("cd: " + rest[0] + ": No such file or directory");
          
        }
        break;

      case MKDIR:
        if(rest != null){      
          String temp_rest[] = rest[0].split("/");
          TreeNode temp_curr = current;

          if(temp_rest.length > 1){
            int i = 0;

            if(temp_rest[1].equals("root")){
              temp_curr = tree.root;
              valid = true;
              i = 2;
            }

            for(; i < temp_rest.length - 1; i++){
              valid = false;

              for(int j = 0; j < temp_curr.children.size(); j++){
                if(temp_curr.children.get(j).getName().equals(temp_rest[i]) && temp_curr.children.get(j).isFolder()){
                  temp_curr = temp_curr.children.get(j);
                  valid = true;
                  break;
                }
              }

              if(!valid){
                System.out.println("mkdir: cannot create directory '" + rest[0] + "': No such file or directory");
                return;
              }
            }

            temp_rest[0] = temp_rest[temp_rest.length-1];
          }

          for(int i = 0; i < temp_curr.children.size(); i++){
            if(temp_curr.children.get(i).getName().equals(temp_rest[0])){
              System.out.println("mkdir: cannot create directory '" + rest[0] + "': File exists");
              break;
            }
          }

          FileDescriptor temp_info = new FileDescriptor(temp_rest[0], true);
          TreeNode temp_node = new TreeNode(temp_curr, temp_info);
          //temp_curr.insertChild(temp_node);
        }

        else{
          System.out.println("mkdir: missing operand");
        }

        break;

      case RMDIR: 
        if(rest != null){
          String temp_rest[] = rest[0].split("/");
          TreeNode temp_curr = current;

          for(int j = 0; j < temp_rest.length; j++){

            found = false;
            for(int i = 0; i < temp_curr.children.size(); i++){
              if(temp_curr.children.get(i).getName().equals(rest[0])){
                TreeNode temp_node = temp_curr.children.get(i);
                temp_node.parent = null;
                temp_curr.removeChild(temp_node);
                temp_node = null;
                found = true;
                break;
              }
            }
          }

          if(!found){
            System.out.println("rmdir: failed to remove '" + rest[0] + "': No such file or directory");
          }
        }

        else{
          System.out.println("rmdir: missing operand");
        }
        break;

      case EDIT:
        if(rest != null){
          String temp_rest[] = rest[0].split("/");
          TreeNode temp_curr = current;

          if(temp_rest.length > 1){
            int i = 0;

            if(temp_rest[1].equals("root")){
              temp_curr = tree.root;
              valid = true;
              i = 2;
            }

            for(; i < temp_rest.length - 1; i++){
              valid = false;

              for(int j = 0; j < temp_curr.children.size(); j++){
                if(temp_curr.children.get(j).getName().equals(temp_rest[i]) && temp_curr.children.get(j).isFolder()){
                  temp_curr = temp_curr.children.get(j);
                  valid = true;
                  break;
                }
              }

              if(!valid){
                System.out.println("edit: cannot create file '" + rest[0] + "': No such directory");
                return;
              }
            }

            temp_rest[0] = temp_rest[temp_rest.length-1]; 
          }

          FileDescriptor new_info;
          TreeNode edit_node = null;

          for(int i = 0; i < temp_curr.children.size(); i++){
            if(temp_curr.children.get(i).getName().equals(temp_rest[0])){
              edit_node = temp_curr.children.get(i);
              System.out.print(edit_node.displayContent());
              break;
            }
          }

          if(edit_node == null){
          	new_info = new FileDescriptor(temp_rest[0], false);
          	edit_node = new TreeNode(temp_curr, new_info);
          }

          Scanner scan = new Scanner(System.in);

          String added = "";
          String temp_add;
          char[] _temp;

          while(true){
          	temp_add = scan.nextLine();
          	_temp = temp_add.toCharArray();

	        if(_temp.length >= 2){
	          	if(_temp[_temp.length-2] == '$' && _temp[_temp.length-1] == '#'){
	          		added += temp_add.substring(0, temp_add.length()-2);
	          		break;
	          	}
	        }

	        added += temp_add;
	        added += "\n";
	      
          }

          edit_node.info.appendContent(added);


        }

        break;

      case CP:
      	break;

      case EXIT: 
        break;

      case LS:
        if(current.children.size() > 0){
          System.out.println();
          for(int i = 0, j = 0; i < current.children.size(); i++, j++){
            System.out.print(current.children.get(i).getName());
            if(current.children.get(i).isFolder()){
              System.out.print("/  ");
            }
            else{
              System.out.print("  ");
            }
            if(j == 4){
              System.out.println();
              j = 0;
            }
          }
          System.out.println();
        }
        break;

      case INVALID:
        System.out.println(rest[0] + ": command not found");
        break;
    }
  }

  public static void main(String[] args) {
    FileSystem fs = new FileSystem();

    //current.info.displayFileInfo();

    Scanner scan = new Scanner(System.in);

    while(fs.command != Command.valueOf("EXIT")){
      fs.printDirectory(fs.current);
      System.out.print("\n$ ");
      String command_line = scan.nextLine(); 

      fs.parseCommand(command_line);

      fs.execute();
      System.out.println();
    }
  }
}