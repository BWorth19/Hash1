
public class TableHash 
{
		public String Key;
		public String Value;
		public int Code;
		public NodeState State = NodeState.regular;
		
		public TableHash(String key, String value, int code) {
			this.Key = key;
			this.Value = value;
			this.Code = code;
			this.State = NodeState.regular;
		}
		
		public static TableHash emptySinceStartNode(int code) {
			TableHash value = new TableHash("", "", code);
			value.State = NodeState.empty_since_start;
			return value;
		}
		
		public static TableHash emptyAfterDeleteNode(int code) {
			TableHash value = new TableHash("", "", code);
			value.State = NodeState.empty_after_delete;
			return value;
		}
		
		enum NodeState{
			empty_since_start, //0
			empty_after_delete, //1
			regular; //2
		}

			public TableHash[] buckets;
			public int Size;
			public int Count;
			private final float Threashold = 0.7f;
			private final int initialSize = 10;
			
			public TableHash() {
				this.buckets = new TableHash[initialSize];
				for(int i = 0; i < buckets.length; i++) {
					buckets[i] = TableHash.emptySinceStartNode(i);
				}
			}
			
			private int GetHash(String key) {
				int hashValue = key.hashCode();
				return hashValue % this.Size;
			}
			
			//add
			public void HashInsert(String key, String value) throws Exception {
				int hashCode = this.GetHash(key);
				TableHash newNode = new TableHash(key, value, hashCode);
				TableHash bucket = this.buckets[hashCode];
				if (bucket.State == NodeState.empty_since_start ||
					bucket.State == NodeState.empty_after_delete) {
					this.buckets[hashCode] = newNode;
					this.Count++;
				} else {
					int nextkey = hashCode;
					do {
						nextkey = (hashCode) + 1 % this.Size;
						bucket = this.buckets[nextkey];
					} while (bucket.State == NodeState.regular &&
							nextkey != hashCode);
					if (nextkey != hashCode) {
						this.buckets[nextkey] = newNode;
						this.Count++;
					} else {
						throw new Exception("The hash table is full.");
					}
				}
			}
			
			
			public TableHash Get(String key) {
				int hashCode = this.GetHash(key);
				TableHash curr = this.buckets[hashCode];
				int nextKey = hashCode;
				if (curr.State == NodeState.empty_since_start) {
					return null;
				} else if (curr.State == NodeState.empty_after_delete || 
						!curr.Key.equals(key)) {
					nextKey = (nextKey + 1) % this.Size;
					curr = this.buckets[nextKey];
					while (curr.State != NodeState.empty_since_start && 
							nextKey != hashCode) {
						if (curr.State == NodeState.empty_after_delete ||
								!curr.Key.equals(key)) {
							nextKey = (nextKey + 1) % this.Size;
							curr = this.buckets[nextKey];
						} else {
							return curr;
						}
					}
					return null;
				} else {
					return curr;
				}

			}
			
			public void Remove(String key) {
				int hashCode = this.GetHash(key);
				int nextkey = hashCode;
				TableHash curr = this.buckets[nextkey];
				if (curr.State == NodeState.empty_since_start) {
					return;
				} else if (curr.State == NodeState.regular &&
						curr.Key.equals(key)) {
					this.buckets[nextkey] = TableHash.emptyAfterDeleteNode(nextkey);
					return;
				} else {
					nextkey = (nextkey + 1) % this.Size;
					curr = this.buckets[nextkey];
					while (curr.State == NodeState.empty_after_delete ||
							(curr.State == NodeState.regular && curr.Key
							!= key) || nextkey == hashCode){
						nextkey = (nextkey + 1) % this.Size;
						curr = this.buckets[nextkey];
					}
					if (nextkey == hashCode) {
						return;
					} else if (curr.State == NodeState.empty_since_start) {
						return;
					} else {
						this.buckets[nextkey] = TableHash.emptyAfterDeleteNode(nextkey);
						return;
					}
				}
			}
			
			
			void Resize() throws Exception {
				if (!this.IsChuncky()) {
					return;
				}
				TableHash[] originalBuckets = this.buckets;
				this.Count = 0;
				this.Size = this.Size * 2;
				this.buckets = new TableHash[this.Size];
				for(int i = 0; i < this.Size; i++) {
					this.buckets[i] = TableHash.emptySinceStartNode(i);
				}
				for(int i = 0; i < originalBuckets.length; i++) {
					TableHash curr = originalBuckets[i];
					if (curr.State == NodeState.regular) {
						this.HashInsert(curr.Key, curr.Value);
					}
				}
			}
			
			private boolean IsChuncky() {
				return (float)this.Count / (float)this.Size >= this.Threashold;
			}

}
