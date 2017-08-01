package assignment1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TxHandler {
	private final UTXOPool copyOfUtxoPool;
	private final UTXOPool masterUtxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    		masterUtxoPool = utxoPool;
        copyOfUtxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        return allOuputsClaimedByTxAreInTheCurrentPool(tx)
        		&& signaturesOnInputsAreValid(tx)
        		&& utxoIsNotClaimedMoreThanOnce(tx)
        		&& allOutputValuesAreNonNegative(tx)
        		&& sumOfAllInputsGreaterOrEqualToOutput(tx);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    		ArrayList<Transaction> validTransactions = new ArrayList<>();
        for(Transaction currentTransaction : possibleTxs) {
        		if(isValidTx(currentTransaction)) {
        			for(UTXO utxoToRemove : getUtxosToBeRemoved(currentTransaction)) {
        				masterUtxoPool.removeUTXO(utxoToRemove);
        			}
        			validTransactions.add(currentTransaction);
        		}
        }
        return validTransactions.toArray(new Transaction[validTransactions.size()]);
    }
    
    private boolean allOuputsClaimedByTxAreInTheCurrentPool(final Transaction tx) {
    		//iterate over inputs creating UTXO object and check if the UTXO object exists in the pool
    		for(int i = 0; i < tx.numInputs(); i++) {
			Transaction.Input input = tx.getInput(i);
			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
			
			if(!copyOfUtxoPool.contains(utxoFromInput)) {
				return false;
			}
		}
    		return true;
    }
    
    private boolean signaturesOnInputsAreValid(final Transaction tx) {
    		boolean isVerified = true;
    		for(int i = 0; i < tx.numInputs(); i++) {
    			Transaction.Input input = tx.getInput(i);
    			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
    			Transaction.Output output = copyOfUtxoPool.getTxOutput(utxoFromInput);
    			byte[] message = tx.getRawDataToSign(i);
    			isVerified = isVerified && Crypto.verifySignature(output.address, message, input.signature);
    		}
    		return isVerified;
    }
    
    private boolean utxoIsNotClaimedMoreThanOnce(final Transaction tx) {
    	  HashSet<UTXO> consumedUtxos = new HashSet<>();
    	  for(int i = 0; i < tx.numInputs(); i++) {
  			Transaction.Input input = tx.getInput(i);
  			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
  			if(consumedUtxos.contains(utxoFromInput)) {
  				return false;
  			}else {
  				consumedUtxos.add(utxoFromInput);
  			}
  		}
    	  return true;
    }
    
    private boolean allOutputValuesAreNonNegative(final Transaction tx) {
    		for(int i = 0; i < tx.numOutputs(); i++) {
    			Transaction.Output output = tx.getOutput(i);
    			if(output.value < 0) {
    				return false;
    			}
    		}
    		return true;
    }
    
    private boolean sumOfAllInputsGreaterOrEqualToOutput(final Transaction tx) {
    		Double sumOfInputs = 0.0;
    		Double sumOfOutputs = 0.0;
    		
    		//get sum of inputs by getting value from Transaction output mapped to UTXO
    		for(int inputIndex = 0; inputIndex < tx.numInputs(); inputIndex++) {
    			Transaction.Input input = tx.getInput(inputIndex);
    			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
    			Transaction.Output output = copyOfUtxoPool.getTxOutput(utxoFromInput);
    			sumOfInputs += output.value;
    		}
    		
    		//get sum of outputs that are part of the transaction
    		for(int outputIndex = 0; outputIndex < tx.numOutputs(); outputIndex++) {
    			Transaction.Output output = tx.getOutput(outputIndex);
    			sumOfOutputs += output.value;
    		}
    		
    		return sumOfInputs >= sumOfOutputs;
    }
    
    private ArrayList<UTXO> getUtxosToBeRemoved(final Transaction tx) {
    		ArrayList<UTXO> utxosToBeRemoved = new ArrayList<>();
    		for(int inputIndex = 0; inputIndex < tx.numInputs(); inputIndex++) {
    			Transaction.Input input = tx.getInput(inputIndex);
    			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
    			utxosToBeRemoved.add(utxoFromInput);
    		}
    		return utxosToBeRemoved;
    }
    
    /*private ArrayList<UTXO> getUtxosToBeAdded(final Transaction tx){
    	 	ArrayList<UTXO> utxosToBeAdded = new ArrayList<>();
    	 	for(int inputIndex = 0; inputIndex < tx.numInputs(); inputIndex++) {
    			Transaction.Input input = tx.getInput(inputIndex);
    			UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
    			Transaction.Output previousOutput = copyOfUtxoPool.getTxOutput(utxoFromInput);
    			Transaction.Output currentOutput = tx.getOutput(inputIndex);
    		}
    }*/

}
