package com.healthcare.keymanagement.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 5.0.3.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class KeyLifecycleRegistry extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b50335f5f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061142d8061005b5f395ff3fe608060405234801561000f575f5ffd5b5060043610610060575f3560e01c8063125f89741461006457806349d66f16146100825780638da5cb5b1461009e578063ad10faf4146100bc578063b8025bab146100ec578063f2fde38b14610121575b5f5ffd5b61006c61013d565b6040516100799190610883565b60405180910390f35b61009c60048036038101906100979190610985565b610143565b005b6100a66104a7565b6040516100b39190610a48565b60405180910390f35b6100d660048036038101906100d19190610a61565b6104cb565b6040516100e39190610883565b60405180910390f35b61010660048036038101906101019190610aac565b610505565b60405161011896959493929190610bfb565b60405180910390f35b61013b60048036038101906101369190610c8b565b61072d565b005b60025481565b5f5f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146101d1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101c890610d26565b60405180910390fd5b5f8585905011610216576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161020d90610d8e565b60405180910390fd5b5f8311610258576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161024f90610e1c565b60405180910390fd5b5f5f1b810361029c576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161029390610e84565b60405180910390fd5b5f85856040516102ad929190610ede565b6040518091039020905060015f8281526020019081526020015f206040518060c0016040528088888080601f0160208091040260200160405190810160405280939291908181526020018383808284375f81840152601f19601f82011690508083019250505050505050815260200186815260200185600581111561033557610334610b79565b5b81526020018481526020014281526020013373ffffffffffffffffffffffffffffffffffffffff16815250908060018154018082558091505060019003905f5260205f2090600602015f909190919091505f820151815f0190816103999190611131565b50602082015181600101556040820151816002015f6101000a81548160ff021916908360058111156103ce576103cd610b79565b5b0217905550606082015181600301556080820151816004015560a0820151816005015f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505060025f8154809291906104419061122d565b91905055503373ffffffffffffffffffffffffffffffffffffffff16817fae69c03ab5be1a47ebeeaddd3837bc6dcb0a240d73189375fcf92e0a85e7c937888888888842604051610497969594939291906112a0565b60405180910390a3505050505050565b5f5f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b5f5f83836040516104dd929190610ede565b6040518091039020905060015f8281526020019081526020015f208054905091505092915050565b60605f5f5f5f5f5f898960405161051d929190610ede565b6040518091039020905060015f8281526020019081526020015f2080549050881061057d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161057490611344565b60405180910390fd5b5f60015f8381526020019081526020015f2089815481106105a1576105a0611362565b5b905f5260205f2090600602016040518060c00160405290815f820180546105c790610f50565b80601f01602080910402602001604051908101604052809291908181526020018280546105f390610f50565b801561063e5780601f106106155761010080835404028352916020019161063e565b820191905f5260205f20905b81548152906001019060200180831161062157829003601f168201915b5050505050815260200160018201548152602001600282015f9054906101000a900460ff16600581111561067557610674610b79565b5b600581111561068757610686610b79565b5b81526020016003820154815260200160048201548152602001600582015f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815250509050805f015181602001518260400151836060015184608001518560a00151975097509750975097509750505093975093979195509350565b5f5f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146107bb576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016107b290610d26565b60405180910390fd5b5f73ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610829576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610820906113d9565b60405180910390fd5b805f5f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b5f819050919050565b61087d8161086b565b82525050565b5f6020820190506108965f830184610874565b92915050565b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f5f83601f8401126108c5576108c46108a4565b5b8235905067ffffffffffffffff8111156108e2576108e16108a8565b5b6020830191508360018202830111156108fe576108fd6108ac565b5b9250929050565b61090e8161086b565b8114610918575f5ffd5b50565b5f8135905061092981610905565b92915050565b6006811061093b575f5ffd5b50565b5f8135905061094c8161092f565b92915050565b5f819050919050565b61096481610952565b811461096e575f5ffd5b50565b5f8135905061097f8161095b565b92915050565b5f5f5f5f5f6080868803121561099e5761099d61089c565b5b5f86013567ffffffffffffffff8111156109bb576109ba6108a0565b5b6109c7888289016108b0565b955095505060206109da8882890161091b565b93505060406109eb8882890161093e565b92505060606109fc88828901610971565b9150509295509295909350565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610a3282610a09565b9050919050565b610a4281610a28565b82525050565b5f602082019050610a5b5f830184610a39565b92915050565b5f5f60208385031215610a7757610a7661089c565b5b5f83013567ffffffffffffffff811115610a9457610a936108a0565b5b610aa0858286016108b0565b92509250509250929050565b5f5f5f60408486031215610ac357610ac261089c565b5b5f84013567ffffffffffffffff811115610ae057610adf6108a0565b5b610aec868287016108b0565b93509350506020610aff8682870161091b565b9150509250925092565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f601f19601f8301169050919050565b5f610b4b82610b09565b610b558185610b13565b9350610b65818560208601610b23565b610b6e81610b31565b840191505092915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602160045260245ffd5b60068110610bb757610bb6610b79565b5b50565b5f819050610bc782610ba6565b919050565b5f610bd682610bba565b9050919050565b610be681610bcc565b82525050565b610bf581610952565b82525050565b5f60c0820190508181035f830152610c138189610b41565b9050610c226020830188610874565b610c2f6040830187610bdd565b610c3c6060830186610bec565b610c496080830185610874565b610c5660a0830184610a39565b979650505050505050565b610c6a81610a28565b8114610c74575f5ffd5b50565b5f81359050610c8581610c61565b92915050565b5f60208284031215610ca057610c9f61089c565b5b5f610cad84828501610c77565b91505092915050565b7f4f6e6c79206f776e65722063616e207265636f7264206c6966656379636c65205f8201527f6576656e74730000000000000000000000000000000000000000000000000000602082015250565b5f610d10602683610b13565b9150610d1b82610cb6565b604082019050919050565b5f6020820190508181035f830152610d3d81610d04565b9050919050565b7f4b65792049442063616e6e6f7420626520656d707479000000000000000000005f82015250565b5f610d78601683610b13565b9150610d8382610d44565b602082019050919050565b5f6020820190508181035f830152610da581610d6c565b9050919050565b7f56657273696f6e206d7573742062652067726561746572207468616e207a65725f8201527f6f00000000000000000000000000000000000000000000000000000000000000602082015250565b5f610e06602183610b13565b9150610e1182610dac565b604082019050919050565b5f6020820190508181035f830152610e3381610dfa565b9050919050565b7f5265636f726420686173682063616e6e6f7420626520656d70747900000000005f82015250565b5f610e6e601b83610b13565b9150610e7982610e3a565b602082019050919050565b5f6020820190508181035f830152610e9b81610e62565b9050919050565b5f81905092915050565b828183375f83830152505050565b5f610ec58385610ea2565b9350610ed2838584610eac565b82840190509392505050565b5f610eea828486610eba565b91508190509392505050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610f6757607f821691505b602082108103610f7a57610f79610f23565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302610fdc7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610fa1565b610fe68683610fa1565b95508019841693508086168417925050509392505050565b5f819050919050565b5f61102161101c6110178461086b565b610ffe565b61086b565b9050919050565b5f819050919050565b61103a83611007565b61104e61104682611028565b848454610fad565b825550505050565b5f5f905090565b611065611056565b611070818484611031565b505050565b5f5b828110156110965761108b5f82840161105d565b600181019050611077565b505050565b601f8211156110e957828211156110e8576110b581610f80565b6110be83610f92565b6110c785610f92565b60208610156110d4575f90505b8083016110e382840382611075565b505050505b5b505050565b5f82821c905092915050565b5f6111095f19846008026110ee565b1980831691505092915050565b5f61112183836110fa565b9150826002028217905092915050565b61113a82610b09565b67ffffffffffffffff81111561115357611152610ef6565b5b61115d8254610f50565b61116882828561109b565b5f60209050601f831160018114611199575f8415611187578287015190505b6111918582611116565b8655506111f8565b601f1984166111a786610f80565b5f5b828110156111ce578489015182556001820191506020850194506020810190506111a9565b868310156111eb57848901516111e7601f8916826110fa565b8355505b6001600288020188555050505b505050505050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f6112378261086b565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff820361126957611268611200565b5b600182019050919050565b5f61127f8385610b13565b935061128c838584610eac565b61129583610b31565b840190509392505050565b5f60a0820190508181035f8301526112b981888a611274565b90506112c86020830187610874565b6112d56040830186610bdd565b6112e26060830185610bec565b6112ef6080830184610874565b979650505050505050565b7f4576656e7420696e646578206f7574206f662072616e676500000000000000005f82015250565b5f61132e601883610b13565b9150611339826112fa565b602082019050919050565b5f6020820190508181035f83015261135b81611322565b9050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b7f4e6577206f776e65722063616e6e6f74206265207a65726f20616464726573735f82015250565b5f6113c3602083610b13565b91506113ce8261138f565b602082019050919050565b5f6020820190508181035f8301526113f0816113b7565b905091905056fea2646970667358221220bcaa66eddf974daaee8529d8b5396cf996db700f1592670ebc972176b328c37064736f6c63430008220033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETEVENTCOUNT = "getEventCount";

    public static final String FUNC_GETKEYEVENT = "getKeyEvent";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RECORDKEYEVENT = "recordKeyEvent";

    public static final String FUNC_TOTALRECORDS = "totalRecords";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event KEYLIFECYCLERECORDED_EVENT = new Event("KeyLifecycleRecorded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected KeyLifecycleRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected KeyLifecycleRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected KeyLifecycleRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected KeyLifecycleRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<KeyLifecycleRecordedEventResponse> getKeyLifecycleRecordedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(KEYLIFECYCLERECORDED_EVENT, transactionReceipt);
        ArrayList<KeyLifecycleRecordedEventResponse> responses = new ArrayList<KeyLifecycleRecordedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            KeyLifecycleRecordedEventResponse typedResponse = new KeyLifecycleRecordedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.keyIdHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.recordedBy = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.keyId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.eventType = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.recordHash = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static KeyLifecycleRecordedEventResponse getKeyLifecycleRecordedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(KEYLIFECYCLERECORDED_EVENT, log);
        KeyLifecycleRecordedEventResponse typedResponse = new KeyLifecycleRecordedEventResponse();
        typedResponse.log = log;
        typedResponse.keyIdHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.recordedBy = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.keyId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.eventType = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.recordHash = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        return typedResponse;
    }

    public Flowable<KeyLifecycleRecordedEventResponse> keyLifecycleRecordedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getKeyLifecycleRecordedEventFromLog(log));
    }

    public Flowable<KeyLifecycleRecordedEventResponse> keyLifecycleRecordedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(KEYLIFECYCLERECORDED_EVENT));
        return keyLifecycleRecordedEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> getEventCount(String keyId) {
        final Function function = new Function(FUNC_GETEVENTCOUNT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(keyId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String>> getKeyEvent(
            String keyId, BigInteger index) {
        final Function function = new Function(FUNC_GETKEYEVENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(keyId), 
                new org.web3j.abi.datatypes.generated.Uint256(index)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String>>(function,
                new Callable<Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String>>() {
                    @Override
                    public Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String> call()
                            throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (byte[]) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (String) results.get(5).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> recordKeyEvent(String keyId, BigInteger version,
            BigInteger eventType, byte[] recordHash) {
        final Function function = new Function(
                FUNC_RECORDKEYEVENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(keyId), 
                new org.web3j.abi.datatypes.generated.Uint256(version), 
                new org.web3j.abi.datatypes.generated.Uint8(eventType), 
                new org.web3j.abi.datatypes.generated.Bytes32(recordHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> totalRecords() {
        final Function function = new Function(FUNC_TOTALRECORDS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static KeyLifecycleRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new KeyLifecycleRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static KeyLifecycleRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new KeyLifecycleRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static KeyLifecycleRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new KeyLifecycleRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static KeyLifecycleRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new KeyLifecycleRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<KeyLifecycleRegistry> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(KeyLifecycleRegistry.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<KeyLifecycleRegistry> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(KeyLifecycleRegistry.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<KeyLifecycleRegistry> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(KeyLifecycleRegistry.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<KeyLifecycleRegistry> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(KeyLifecycleRegistry.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class KeyLifecycleRecordedEventResponse extends BaseEventResponse {
        public byte[] keyIdHash;

        public String recordedBy;

        public String keyId;

        public BigInteger version;

        public BigInteger eventType;

        public byte[] recordHash;

        public BigInteger timestamp;
    }
}
