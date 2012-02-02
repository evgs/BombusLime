package org.bombusim.lime.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.bombusim.xmpp.XmppAccount;

import android.content.Context;

public class AccountsFactory {
	private static final String FILE_ACTIVE_ACCOUNT_INDEX = "activeAccount.idx";

	private AccountsFactory() {}
	
	public static ArrayList<XmppAccount> loadAccounts(Context context) {
		ArrayList<XmppAccount> list = new ArrayList<XmppAccount>();
		
		AccountDbAdapter dba = new AccountDbAdapter(context);
		dba.open();
		long[] indexes = dba.getAccountIndexes();
		
		if (indexes != null)   for (long index : indexes) {
			XmppAccount a=dba.getAccount(index);
			a._id = index;
			list.add(a);
		}
		
		if (list.isEmpty()) {
			list.add(new XmppAccount());
		}
		
		dba.close();
		
		return list;
	}

	public static void saveAccount(Context context, XmppAccount account ) {
		AccountDbAdapter dba = new AccountDbAdapter(context);

		dba.open();
		
		account._id = dba.putAccount(account, account._id);
		
		dba.close();
		
	}
	
	public static int getActiveAccountIndex(Context context) {
		FileInputStream is;
		try {
			is = context.openFileInput(FILE_ACTIVE_ACCOUNT_INDEX);
			int result = is.read();
			is.close();
			
			return result;
		} catch (IOException e) {}
		return 0;
	}
	
	public static void saveActiveAccountIndex(Context context, int index) {
		FileOutputStream os;
		try {
			os = context.openFileOutput(FILE_ACTIVE_ACCOUNT_INDEX, Context.MODE_PRIVATE);
			os.write((byte)index);
			os.close();
		} catch (IOException e) {}
	}

	public static int addNew(ArrayList<XmppAccount> accounts) {
		accounts.add(new XmppAccount());
		return accounts.size()-1;
	}

	public static void removeAccount(Context context, XmppAccount account) {
		AccountDbAdapter dba = new AccountDbAdapter(context);

		dba.open();
		
		dba.removeAccount(account._id);
		
		dba.close();
		
	}

	public static String[] getLabels(ArrayList<XmppAccount> accounts) {
		String labels[] = new String[accounts.size()];
		
		for (int i=0; i<accounts.size(); i++) {
			labels[i]=accounts.get(i).userJid;
		}
		return labels;
	}
}
